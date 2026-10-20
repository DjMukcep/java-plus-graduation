package ru.practicum.entity.event;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import practicum.StatsClient;
import practicum.StatsRequest;

import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.dto.event.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.category.CategoryMapper;
import ru.practicum.entity.category.CategoryService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.internal.request.RequestClient;
import ru.practicum.internal.user.UserClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final StatsClient statsServerClient;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final RequestClient requestClient;
    private final UserClient userClient;

    private static final String APP_NAME = "ewm-main-service";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ---------- Private ----------

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        UserDto user = userClient.getUserById(userId);
        CategoryDto categoryDto = categoryService.findById(dto.getCategory());
        Category category = CategoryMapper.toEntity(categoryDto);

        Event event = EventMapper.mapToEntity(dto, category, user);
        event = eventRepository.save(event);

        var result = EventMapper.toEventFullDto(event, 0L, 0L, user);
        log.info("Новое событие: {}", EventMapper.toLogEvent(result));

        return result;
    }

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        UserDto user = userClient.getUserById(userId);

        Event event = getEventById(eventId);

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);
        long confirmedRequests = getEventRequestsCount(eventId);

        return EventMapper.toEventFullDto(event, confirmedRequests, views, user);
    }

    @Override
    public List<Event> getByIds(Collection<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Pageable pageable) {
        UserDto user = userClient.getUserById(userId);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Long> eventsRequestsCount = getEventsRequests(events);

        return EventMapper.toEventShortDtos(events, eventsRequestsCount, viewsMap, user);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByCreatorId(EventParamDto eventParamDto, UpdateEventUserRequest request) {
        Long userId = eventParamDto.getUserId();
        UserDto user = userClient.getUserById(userId);
        Event event = getEventById(eventParamDto.getEventId());
        checkUserIsEventInitiator(userId, event);
        checkEventUpdateConditions(event, request);

        updateEventFromUserRequest(request, event);

        long requestsCount = getEventRequestsCount(event.getId());
        Long views = getViewsMap(Collections.singletonList(event)).getOrDefault(event.getId(), 0L);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, requestsCount, views, user);
        log.info("Событие обновлено создателем: {}", EventMapper.toLogEvent(eventFullDto));

        return eventFullDto;
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByCreatorId(Long userId, Long eventId) {
        userClient.checkUserExists(userId);
        Event event = getEventById(eventId);
        checkUserIsEventInitiator(userId, event);

        return requestClient.getUserRequestsByEventId(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestsStatus(
            EventParamDto eventParamDto,
            EventRequestStatusUpdateRequest request) {

        Long userId = eventParamDto.getUserId();
        Long eventId = eventParamDto.getEventId();

        Event event = getEventById(eventId);
        userClient.checkUserExists(userId);
        checkUserIsEventInitiator(userId, event);

        List<ParticipationRequestDto> requests =
                getRequestsForUpdate(eventId, request.getRequestIds());

        long remainedRequests = getRemainedRequests(event);

        validateConfirmationAllowed(event, request.getStatus(), remainedRequests);

        RequestsByStatus requestsByStatus =
                distributeRequests(requests, request.getStatus(), remainedRequests);

        List<ParticipationRequestDto> rejected =
                updateRequestsStatus(requestsByStatus.rejected());

        List<ParticipationRequestDto> confirmed =
                updateRequestsStatus(requestsByStatus.confirmed());

        var result = new EventRequestStatusUpdateResult(confirmed, rejected);

        log.info("Обновлены статусы заявок на событие: {}", result);

        return result;
    }

    private List<ParticipationRequestDto> getRequestsForUpdate(
            Long eventId, Set<Long> requestIds) {

        return requestClient.getUserRequestsByUserIdsAndEventId(
                new RequestParamDto(eventId, requestIds));
    }

    private long getRemainedRequests(Event event) {
        return event.getParticipantLimit() - getEventRequestsCount(event.getId());
    }

    private void validateConfirmationAllowed(
            Event event, RequestStatus targetStatus, long remainedRequests) {

        if (targetStatus != RequestStatus.CONFIRMED) {
            return;
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException(
                    "Confirmation is not required or allowed for this event configuration.");
        }

        if (remainedRequests <= 0L) {
            throw new ConflictException("The participant limit has been reached");
        }
    }

    private record RequestsByStatus(
            StatusRequestsDto confirmed,
            StatusRequestsDto rejected) {
    }

    private RequestsByStatus distributeRequests(
            List<ParticipationRequestDto> requests,
            RequestStatus targetStatus,
            long remainedRequests) {

        var confirmed = new StatusRequestsDto(
                new HashSet<>(), RequestStatus.CONFIRMED);

        var rejected = new StatusRequestsDto(
                new HashSet<>(), RequestStatus.REJECTED);

        for (ParticipationRequestDto request : requests) {
            validatePendingStatus(request);

            if (targetStatus == RequestStatus.REJECTED || remainedRequests <= 0L) {
                rejected.getRequestIds().add(request.getId());
            } else {
                confirmed.getRequestIds().add(request.getId());
                remainedRequests--;
            }
        }

        return new RequestsByStatus(confirmed, rejected);
    }

    private void validatePendingStatus(ParticipationRequestDto request) {
        if (!request.getStatus().equalsIgnoreCase(RequestStatus.PENDING.name())) {
            throw new ConflictException(String.format(
                    "Request must have status PENDING. Request with id=%d has status %s",
                    request.getId(),
                    request.getStatus()));
        }
    }

    private List<ParticipationRequestDto> updateRequestsStatus(
            StatusRequestsDto requests) {

        if (requests.getRequestIds().isEmpty()) {
            return new ArrayList<>();
        }

        return requestClient.updateStatusRequests(requests);
    }

    // ---------- Admin ----------
    @Override
    public List<EventFullDto> getEventsByAdmin(EventAdminParamDto params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Predicate predicate = buildAdminPredicate(params);

        List<Event> events = predicate == null ?
                eventRepository.findAll(pageable).getContent() :
                eventRepository.findAll(predicate, pageable).getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long,UserDto> users = getUsers(events);
        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Long> eventRequestsCount = getEventsRequests(events);

        return events.stream()
                .map(event -> {
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    Long requestsCount = eventRequestsCount.getOrDefault(event.getId(), 0L);
                    UserDto user = users.get(event.getInitiatorId());
                    return EventMapper.toEventFullDto(event, requestsCount, views, user);
                })
                .toList();
    }

    private Map<Long, UserDto> getUsers(List<Event> events) {
        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        UserParamDto userParamDto = new UserParamDto();
        userParamDto.setIds(userIds);
        userParamDto.setSize(userIds.size());

        return userClient.getUsers(userParamDto).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventById(eventId);
        UserDto user = userClient.getUserById(event.getInitiatorId());
        updateEventFromAdminRequest(request, event);

        applyStateAction(event, request.getStateAction());

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);
        long confirmedRequests = getEventRequestsCount(event.getId());

        var result = EventMapper.toEventFullDto(event, confirmedRequests, views, user);
        log.info("Событие обновлено админом: {}", EventMapper.toLogEvent(result));

        return result;
    }

    private void applyStateAction(Event event, AdminStateAction stateAction) {
        if (stateAction == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        switch (stateAction) {
            case AdminStateAction.PUBLISH_EVENT -> {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }

                if (event.getEventDate().isBefore(now.plusHours(1))) {
                    throw new ConflictException("Cannot publish the event because event date is less than 1 hour from now.");
                }

                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(now);
            }
            case AdminStateAction.REJECT_EVENT -> {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject the event because it's already published.");
                }

                event.setState(EventState.CANCELED);
            }
            default -> throw new ConflictException("Invalid state action: " + stateAction);
        }
    }

    // ---------- Public ----------

    @Override
    public List<EventShortDto> getPublishedEvents(EventPublicParamDto params, HttpServletRequest request) {
        validateDateRange(params.getRangeStart(), params.getRangeEnd());

        LocalDateTime rangeStart = params.getRangeStart() != null ? params.getRangeStart() : LocalDateTime.now();

        Pageable pageable = buildPageable(params.getSort(), params.getFrom(), params.getSize());
        Predicate predicate = buildPublicPredicate(params, rangeStart);

        List<Event> events = predicate == null ?
                eventRepository.findAll(pageable).getContent() :
                eventRepository.findAll(predicate, pageable).getContent();

        if (events.isEmpty()) {
            sendHit(request);
            return Collections.emptyList();
        }

        sendHit(request);

        Map<Long, UserDto> users = getUsers(events);
        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Long> eventRequestsCount = getEventsRequests(events);

        List<EventShortDto> result = new ArrayList<>();

        for (Event event : events) {
            Long views = viewsMap.getOrDefault(event.getId(), 0L);
            Long requestsCount = eventRequestsCount.getOrDefault(event.getId(), 0L);
            UserDto user = users.get(event.getInitiatorId());

            if (params.isOnlyAvailable()) {
                long remainedRequests = event.getParticipantLimit() == 0 ?
                        1 : event.getParticipantLimit() - requestsCount;
                if (remainedRequests > 0) {

                    result.add(EventMapper.toEventShortDto(event, requestsCount, views, user));
                }
                continue;
            }
            result.add(EventMapper.toEventShortDto(event, requestsCount, views, user));
        }

        if (SortBy.VIEWS == params.getSort()) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return result;
    }

    @Override
    public EventFullDto getPublishedEventById(Long id, HttpServletRequest request) {
        Event event = findEventByIdAndState(id, EventState.PUBLISHED);

        sendHit(request);

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);
        long confirmedRequests = requestClient.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        UserDto user = userClient.getUserById(event.getInitiatorId());

        return EventMapper.toEventFullDto(event, confirmedRequests, views, user);
    }

    @Override
    public Map<Long, Long> getEventsRequests(List<Event> events) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        List<EventRequestsCountDto> requests = requestClient.countByEventIdsAndStatus(
                new StatusRequestsDto(eventIds, RequestStatus.CONFIRMED));

        return requests.stream()
                .collect(Collectors.toMap(EventRequestsCountDto::eventId, EventRequestsCountDto::count));
    }

    @Override
    public Map<Long, Long> getViewsMap(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        List<ViewStats> stats = statsServerClient.getViewStats(new StatsRequest(
                start.format(formatter),
                LocalDateTime.now().plusYears(1).format(formatter),
                uris, true));

        return stats.stream()
                .collect(Collectors.toMap(
                        vs -> Long.parseLong(vs.getUri().substring("/events/".length())),
                        ViewStats::getHits));
    }

    @Override
    public Event findEventByIdAndState(Long id, EventState state) {
        return  eventRepository.findByIdAndState(id, state)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
    }

    @Override
    public boolean isEventExistsAndPublished(Long eventId) {
        return eventRepository.existsByIdAndState(eventId, EventState.PUBLISHED);
    }

    @Override
    public boolean isEventExist(Long eventId) {
        return eventRepository.existsById( eventId);
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findWithRelationsById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id " + eventId + " not found")
        );
    }

    // ---------- QueryDSL predicates ----------

    private Predicate buildAdminPredicate(EventAdminParamDto params) {
        QEvent event = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();

        if (params.getUsers() != null && !params.getUsers().isEmpty()) {
            builder.and(event.initiatorId.in(params.getUsers()));
        }
        if (params.getStates() != null && !params.getStates().isEmpty()) {
            builder.and(event.state.in(params.getStates().stream()
                    .map(stateStr -> EventState.valueOf(stateStr.toUpperCase()))
                    .toList()));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            builder.and(event.category.id.in(params.getCategories()));
        }
        if (params.getRangeStart() != null) {
            builder.and(event.eventDate.goe(params.getRangeStart()));
        }
        if (params.getRangeEnd() != null) {
            builder.and(event.eventDate.loe(params.getRangeEnd()));
        }
        return builder.getValue();
    }

    private Predicate buildPublicPredicate(EventPublicParamDto params, LocalDateTime rangeStart) {
        QEvent event = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(event.state.eq(EventState.PUBLISHED));

        if (params.getText() != null && !params.getText().isBlank()) {
            String text = params.getText();
            builder.and(new BooleanBuilder()
                    .or(event.annotation.containsIgnoreCase(text))
                    .or(event.description.containsIgnoreCase(text)));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            builder.and(event.category.id.in(params.getCategories()));
        }
        if (params.getPaid() != null) {
            builder.and(event.paid.eq(params.getPaid()));
        }
        if (rangeStart != null) {
            builder.and(event.eventDate.goe(rangeStart));
        }
        if (params.getRangeEnd() != null) {
            builder.and(event.eventDate.loe(params.getRangeEnd()));
        }
        return builder.getValue();
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("Range end must be after range start");
        }
    }

    private Pageable buildPageable(SortBy sort, int from, int size) {
        return SortBy.EVENT_DATE == sort ?
                PageRequest.of(from / size, size, Sort.by("eventDate").ascending())
                : PageRequest.of(from / size, size);
    }

    private void sendHit(HttpServletRequest request) {
        EndpointHit endpointHit = EndpointHit.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
        statsServerClient.hit(endpointHit);
        log.info("На сервер статистики отправлен endpointHit: {}", endpointHit);
    }

    private void updateEventFromUserRequest(UpdateEventUserRequest request, Event event) {
        ofNullable(request.getAnnotation()).ifPresent(event::setAnnotation);
        ofNullable(request.getDescription()).ifPresent(event::setDescription);
        ofNullable(request.getEventDate()).ifPresent(event::setEventDate);
        ofNullable(request.getLocation()).ifPresent(event::setLocation);
        ofNullable(request.getPaid()).ifPresent(event::setPaid);
        ofNullable(request.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        ofNullable(request.getRequestModeration()).ifPresent(event::setRequestModeration);
        ofNullable(request.getTitle()).ifPresent(event::setTitle);

        if (request.getCategory() != null) {
            CategoryDto categoryDto = categoryService.findById(request.getCategory());
            Category category = CategoryMapper.toEntity(categoryDto);
            event.setCategory(category);
        }

        if (request.getStateAction() == UserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        if (request.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }
    }

    public void updateEventFromAdminRequest(UpdateEventAdminRequest request, Event event) {
        if (request.getEventDate() != null &&
                request.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Event date cannot be in the past");
        }

        if (request.getCategory() != null) {
            CategoryDto categoryDto = categoryService.findById(request.getCategory());
            Category category = CategoryMapper.toEntity(categoryDto);
            event.setCategory(category);
        }

        ofNullable(request.getAnnotation()).ifPresent(event::setAnnotation);
        ofNullable(request.getDescription()).ifPresent(event::setDescription);
        ofNullable(request.getEventDate()).ifPresent(event::setEventDate);
        ofNullable(request.getLocation()).ifPresent(event::setLocation);
        ofNullable(request.getPaid()).ifPresent(event::setPaid);
        ofNullable(request.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        ofNullable(request.getRequestModeration()).ifPresent(event::setRequestModeration);
        ofNullable(request.getTitle()).ifPresent(event::setTitle);
    }

    private void checkUserIsEventInitiator(Long userId, Event event) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException(
                    String.format("User with id %d didn't initiate event with id %d", userId, event.getId())
            );
        }
    }

    private void checkEventUpdateConditions(Event event, UpdateEventUserRequest request) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        if (request.getEventDate() != null && !request.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least two hours in the future.");

        }
    }

    private long getEventRequestsCount(Long eventId) {
        return requestClient.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }
}