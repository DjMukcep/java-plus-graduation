package ru.practicum.entity.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EventService {
    // Admin
    List<EventFullDto> getEventsByAdmin(EventAdminParamDto params);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    // Public
    List<EventShortDto> getPublishedEvents(EventPublicParamDto params, HttpServletRequest request);

    EventFullDto getPublishedEventById(Long id, HttpServletRequest request);

    // Private
    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto getById(Long userId, Long eventId);

    List<EventShortDto> getUserEvents(Long userId, Pageable pageable);

    List<Event> getByIds(Collection<Long> ids);

    EventFullDto updateEventByCreatorId(EventParamDto eventParamDto, UpdateEventUserRequest request);

    List<ParticipationRequestDto> getEventRequestsByCreatorId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestsStatus(EventParamDto eventParamDto, EventRequestStatusUpdateRequest request);

    Map<Long, Long> getEventsRequests(List<Event> events);

    Map<Long, Long> getViewsMap(List<Event> events);

    Event findEventByIdAndState(Long id, EventState state);

    boolean isEventExists(Long eventId);
}