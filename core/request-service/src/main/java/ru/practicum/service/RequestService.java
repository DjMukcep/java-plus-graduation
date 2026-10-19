package ru.practicum.service;

import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestParamDto;
import ru.practicum.dto.request.RequestStatusUpdate;
import ru.practicum.entity.Request;
import ru.practicum.dto.request.RequestStatus;

import java.util.List;
import java.util.Set;

public interface RequestService {

    ParticipationRequestDto saveRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    List<EventRequestsCountDto> countByEventIdsAndStatus(Set<Long> ids, RequestStatus status);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByIdsAndEventId(RequestParamDto requestDto);

    List<Request> getRequestsByEventId(Long eventId);

    List<Request> updateStatusRequests(Set<Long> ids, RequestStatus status);
}
