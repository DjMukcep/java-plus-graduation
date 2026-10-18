package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestParamDto;
import ru.practicum.dto.request.RequestStatusUpdate;
import ru.practicum.dto.user.UserDto;

import ru.practicum.entity.Request;
import ru.practicum.dto.request.RequestStatus;

import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.proxy.event.EventClient;
import ru.practicum.proxy.user.UserClient;
import ru.practicum.repository.RequestRepository;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public ParticipationRequestDto saveRequest(Long userId, Long eventId) {
        if (existsByUserIdAndEventId(userId, eventId)) {
            throw new ConflictException("Attempt to send a duplicate request.");
        }

        UserDto user = userClient.getUserById(userId);
        EventDto event = eventClient.getEventById(eventId);
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

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("You can only cancel your own participation requests.");
        }

        request.setStatus(RequestStatus.CANCELED);

        var requestDto = RequestMapper.toRequestDto(request);
        log.info("Заявка на событие отменена: {}", requestDto);

        return requestDto;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userClient.checkUserExists(userId);

        return RequestMapper.toRequestDto(requestRepository.findByRequesterId(userId));
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
    public List<Request> findByIdsAndEventId(RequestParamDto requestDto) {
        return requestRepository.findByIdInAndEventId(requestDto.getIds(), requestDto.getEventId());
    }

    @Override
    public List<Request> getRequestsByEventId(Long eventId) {
        return requestRepository.findByEventId(eventId);
    }

    @Override
    @Transactional
    public Request updateStatus(RequestStatusUpdate requestStatusUpdate) {
        Request request = getRequest(requestStatusUpdate.getRequestId());
        request.setStatus(requestStatusUpdate.getRequestStatus());

        return request;
    }

    private boolean existsByUserIdAndEventId(Long userId, Long eventId) {
        return requestRepository.existsByRequesterIdAndEventId(userId, eventId);
    }

    private Request getRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Request with id " + requestId + " not found.")
        );
    }


    private void checkUserRequestForEvent(EventDto event, Long userId) {
        if (event.initiatorId().equals(userId)) {
            throw new ConflictException("The initiator can't request to join their own event.");
        }
        if (!event.eventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("The Participation in unpublished events is forbidden.");
        }

        if (event.participantLimit() != 0) {
            long requests = countByEventIdAndStatus(event.eventId(), RequestStatus.CONFIRMED);
            if (requests >= event.participantLimit()) {
                throw new ConflictException("The participant limit has been reached.");
            }
        }
    }

}
