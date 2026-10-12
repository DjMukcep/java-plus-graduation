package ru.practicum.entity.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventRepository;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ParticipationRequestDto saveRequest(Long userId, Long eventId) {
        if (existsByUserIdAndEventId(userId, eventId)) {
            throw new ConflictException("Attempt to send a duplicate request.");
        }

        User user = userService.findById(userId);
        Event event = getEvent(eventId);
        checkUserRequestForEvent(event, userId);

        Request request = RequestMapper.toRequest(event, user);
        request = requestRepository.save(request);

        var requestDto = RequestMapper.toRequestDto(request);
        log.info("Новая заявка на событие: {}", requestDto);

        return requestDto;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = getRequest(requestId);

        if (!request.getUser().getId().equals(userId)) {
            throw new ConflictException("You can only cancel your own participation requests.");
        }

        request.setStatus(RequestStatus.CANCELED);

        var requestDto = RequestMapper.toRequestDto(request);
        log.info("Заявка на событие отменена: {}", requestDto);

        return requestDto;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.checkUserExist(userId);

        return RequestMapper.toRequestDto(requestRepository.findByUserId(userId));
    }

    @Override
    public List<EventRequestsCountDto> countByEventIdsAndStatus(Set<Long> ids, RequestStatus status) {
        return requestRepository.countByEventIdInAndStatus(ids, status);
    }

    @Override
    public long countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return requestRepository.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public List<Request> findByIdsAndEventId(Set<Long> ids, Long eventId) {
        return requestRepository.findByIdInAndEventId(ids, eventId);
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsByEventId(Long eventId) {
        return RequestMapper.toRequestDto(requestRepository.findByEventId(eventId));
    }

    private boolean existsByUserIdAndEventId(Long userId, Long eventId) {
        return requestRepository.existsByUserIdAndEventId(userId, eventId);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id " + eventId + " not found.")
        );
    }

    private Request getRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Request with id " + requestId + " not found.")
        );
    }

    private void checkUserRequestForEvent(Event event, Long userId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The initiator can't request to join their own event.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("The Participation in unpublished events is forbidden.");
        }

        if (event.getParticipantLimit() != 0) {
            long requests = countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            if (requests >= event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached.");
            }
        }
    }

}
