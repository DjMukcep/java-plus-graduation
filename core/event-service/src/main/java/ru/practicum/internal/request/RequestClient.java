package ru.practicum.internal.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.dto.request.*;
import ru.practicum.dto.request.RequestStatus;

import java.util.List;

@Validated
@FeignClient(name = "request-service", fallbackFactory = RequestClientFallbackFactory.class)
public interface RequestClient {

    @GetMapping("/users/requests")
    List<ParticipationRequestDto> getUserRequestsByUserIdsAndEventId(
            @SpringQueryMap @Valid RequestParamDto requestDto);

    @GetMapping("/users/requests/{eventId}")
    List<ParticipationRequestDto> getUserRequestsByEventId(@PathVariable @Positive Long eventId);

    @PatchMapping("/users/requests/status/update")
    List<ParticipationRequestDto> updateStatusRequests(@SpringQueryMap StatusRequestsDto statusRequestsDto);

    @GetMapping("/users/requests/{eventId}/count")
    long countByEventIdAndStatus(@PathVariable @Positive Long eventId,
                                 @RequestParam RequestStatus status);

    @GetMapping("/users/requests/count")
    List<EventRequestsCountDto> countByEventIdsAndStatus(
            @SpringQueryMap StatusRequestsDto statusRequestsDto);
}
