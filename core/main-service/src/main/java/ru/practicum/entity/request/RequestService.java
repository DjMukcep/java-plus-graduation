package ru.practicum.entity.request;

import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Set;

public interface RequestService {

    ParticipationRequestDto saveRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    List<EventRequestsCountDto> countByEventIdsAndStatus(Set<Long> ids, RequestStatus status);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByIdsAndEventId(Set<Long> ids, Long eventId);

    List<ParticipationRequestDto> getParticipationRequestsByEventId(Long eventId);
}
