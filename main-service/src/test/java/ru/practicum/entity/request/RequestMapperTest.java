package ru.practicum.entity.request;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    @Test
    void toRequest_shouldCreatePendingRequest_whenModerationEnabled() {
        Event event = Event.builder()
                .id(1L)
                .participantLimit(5)
                .requestModeration(true)
                .build();

        User user = User.builder()
                .id(2L)
                .build();

        Request request = RequestMapper.toRequest(event, user);

        assertNotNull(request);
        assertEquals(event, request.getEvent());
        assertEquals(user, request.getUser());
        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertNotNull(request.getCreatedAt());
    }

    @Test
    void toRequest_shouldCreateConfirmedRequest_whenModerationDisabled() {
        Event event = Event.builder()
                .id(1L)
                .requestModeration(false)
                .build();

        User user = User.builder()
                .id(2L)
                .build();

        Request request = RequestMapper.toRequest(event, user);

        assertEquals(RequestStatus.CONFIRMED, request.getStatus());
    }

    @Test
    void toRequestDto_shouldMapRequestToDto() {
        Event event = Event.builder()
                .id(10L)
                .build();

        User user = User.builder()
                .id(20L)
                .build();

        LocalDateTime createdAt = LocalDateTime.of(
                2026, 1, 1, 12, 30, 45
        );

        Request request = Request.builder()
                .id(100L)
                .event(event)
                .user(user)
                .createdAt(createdAt)
                .status(RequestStatus.CONFIRMED)
                .build();

        ParticipationRequestDto dto = RequestMapper.toRequestDto(request);

        assertEquals(100L, dto.getId());
        assertEquals("2026-01-01 12:30:45", dto.getCreated());
        assertEquals(10L, dto.getEventId());
        assertEquals(20L, dto.getRequesterId());
        assertEquals("CONFIRMED", dto.getStatus());
    }

    @Test
    void toRequestDtos_shouldMapList() {
        Event event = Event.builder()
                .id(1L)
                .build();

        User user = User.builder()
                .id(2L)
                .build();

        Request request1 = Request.builder()
                .id(10L)
                .event(event)
                .user(user)
                .createdAt(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();

        Request request2 = Request.builder()
                .id(20L)
                .event(event)
                .user(user)
                .createdAt(LocalDateTime.now())
                .status(RequestStatus.CONFIRMED)
                .build();

        List<ParticipationRequestDto> result =
                RequestMapper.toRequestDto(List.of(request1, request2));

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(20L, result.get(1).getId());
    }

    @Test
    void toRequestDtos_shouldReturnEmptyList() {
        List<ParticipationRequestDto> result =
                RequestMapper.toRequestDto(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

}