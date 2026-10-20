package ru.practicum.dto.event;

public record EventDto(Long eventId,
                       Long initiatorId,
                       EventState eventState,
                       int participantLimit,
                       boolean reqModeration) {
}
