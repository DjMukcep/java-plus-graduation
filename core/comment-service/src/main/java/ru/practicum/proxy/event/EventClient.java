package ru.practicum.proxy.event;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// потом переименовать на event-service
@Validated
@FeignClient(name = "main-service", fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient {

    @GetMapping("/events/exists/published/{eventId}")
    boolean checkEventExistsAndPublished(@PathVariable @Positive Long eventId);

    @GetMapping("/events/exists/{eventId}")
    boolean checkEventExists(@PathVariable @Positive Long eventId);
}
