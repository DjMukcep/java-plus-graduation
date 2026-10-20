package ru.practicum.internal.event;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.EventDto;

@Validated
@FeignClient(name = "event-service",  fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient {

    @GetMapping("/users/events/{eventId}")
    EventDto getEventById(@PathVariable @Positive Long eventId);
}
