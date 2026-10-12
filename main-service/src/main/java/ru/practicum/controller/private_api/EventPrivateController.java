package ru.practicum.controller.private_api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.event.EventService;

import java.util.List;


@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class EventPrivateController {

    private final EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable @Positive Long userId,
                               @RequestBody @Valid NewEventDto dto) {
        return eventService.create(userId, dto);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getById(@PathVariable @Positive Long userId,
                                @PathVariable @Positive Long eventId) {
        return eventService.getById(userId, eventId);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getUserEvents(@PathVariable @Positive Long userId,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventService.getUserEvents(userId, pageable);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEventByCreatorId(@PathVariable @Positive Long userId,
                                               @PathVariable @Positive Long eventId,
                                               @RequestBody @Valid UpdateEventUserRequest request) {
        return eventService.updateEventByCreatorId(new EventParamDto(userId, eventId), request);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequestsByCreatorId(@PathVariable @Positive Long userId,
                                                                     @PathVariable @Positive Long eventId) {
        return eventService.getEventRequestsByCreatorId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestsStatus(@PathVariable @Positive Long userId,
                                                                    @PathVariable @Positive Long eventId,
                                                                    @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        return eventService.updateEventRequestsStatus(new EventParamDto(userId, eventId), request);
    }
}

