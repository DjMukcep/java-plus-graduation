package ru.practicum.service;

import org.junit.jupiter.api.Test;
import ru.practicum.EndpointHit;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


class StatMapperTest {

    @Test
    void toHitEntity_shouldMapCorrectly() {

        EndpointHit dto = EndpointHit.builder()
                .app("app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("2026-06-18 12:34:56")
                .build();

        EndpointHitEntity entity = StatMapper.toHitEntity(dto);

        assertThat(entity.getApp(), equalTo("app"));
        assertThat(entity.getUri(), equalTo("/test"));
        assertThat(entity.getIp(), equalTo("127.0.0.1"));
        assertThat(entity.getTimestamp(),
                equalTo(LocalDateTime.of(2026, 6, 18, 12, 34, 56)));
    }

    @Test
    void toHitEntity_shouldThrowException_whenTimestampIsInvalid() {

        EndpointHit dto = EndpointHit.builder()
                .app("app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("invalid-date")
                .build();

        assertThatThrownBy(() -> StatMapper.toHitEntity(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid time format: invalid-date");
    }
}
