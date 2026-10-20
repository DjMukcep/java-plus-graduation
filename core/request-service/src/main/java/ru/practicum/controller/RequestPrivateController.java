package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.dto.request.*;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.service.RequestService;

import java.util.List;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam @Positive Long eventId) {
        return requestService.saveRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        return requestService.getUserRequests(userId);
    }

    //----------proxy-----------//
    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequestsByUserIdsAndEventId(
            @ModelAttribute RequestParamDto requestDto) {
        return RequestMapper.toRequestDto(requestService.findByIdsAndEventId(requestDto));
    }

    @GetMapping("/requests/{eventId}")
    public List<ParticipationRequestDto> getUserRequestsByEventId(
            @PathVariable @Positive Long eventId) {
        return RequestMapper.toRequestDto(requestService.getRequestsByEventId(eventId));
    }

    @PatchMapping("/requests/status/update")
    public List<ParticipationRequestDto> updateStatusRequests(
            @ModelAttribute StatusRequestsDto statusRequestsDto) {
        return RequestMapper.toRequestDto(requestService.updateStatusRequests(
                statusRequestsDto.getRequestIds(), statusRequestsDto.getRequestStatus()
        ));
    }

    @GetMapping("/requests/{eventId}/count")
    public long countByEventIdAndStatus(@PathVariable @Positive Long eventId,
                                        @RequestParam RequestStatus status) {
        return requestService.countByEventIdAndStatus(eventId, status);
    }

    @GetMapping("/requests/count")
    public List<EventRequestsCountDto> countByEventIdsAndStatus(
            @ModelAttribute StatusRequestsDto statusRequestsDto) {
        return requestService.countByEventIdsAndStatus(
                statusRequestsDto.getRequestIds(), statusRequestsDto.getRequestStatus());
    }
}
