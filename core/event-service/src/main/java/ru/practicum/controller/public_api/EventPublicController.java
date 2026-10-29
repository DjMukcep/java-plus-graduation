package ru.practicum.controller.public_api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicParamDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.RecommendedEvent;
import ru.practicum.entity.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@ModelAttribute @Valid EventPublicParamDto params,
                                         HttpServletRequest request) {
        return eventService.getPublishedEvents(params, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable @Positive Long id,
                                     @RequestHeader("X-EWM-USER-ID") Long userId) {
        return eventService.getPublishedEventById(id, userId);
    }

    @GetMapping("/recommendations")
    public List<RecommendedEvent> getRecommendedEvents(@RequestHeader("X-EWM-USER-ID") Long userId,
                                                       @RequestParam Integer maxResults) {
        return eventService.getRecommendedEvents(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(
            @PathVariable @Positive Long eventId,
            @RequestHeader("X-EWM-USER-ID") Long userId) {
        eventService.likeEvent(eventId, userId);
    }

    //Internal calls
    @GetMapping("/exists/published/{eventId}")
    public boolean checkEventExistsAndPublished(@PathVariable @Positive Long eventId) {
        return eventService.isEventExistsAndPublished(eventId);
    }

    @GetMapping("/exists/{eventId}")
    public boolean checkEventExists(@PathVariable @Positive Long eventId) {
        return eventService.isEventExist(eventId);
    }
}