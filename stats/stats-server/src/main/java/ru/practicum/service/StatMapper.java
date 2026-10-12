package ru.practicum.service;

import lombok.experimental.UtilityClass;
import ru.practicum.EndpointHit;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class StatMapper {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHitEntity toHitEntity(EndpointHit endpointHit) {
        LocalDateTime time;

        try {
            time = LocalDateTime.parse(endpointHit.getTimestamp(), formatter);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid time format: " + endpointHit.getTimestamp());
        }

        return EndpointHitEntity.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(time)
                .build();
    }
}
