package ru.practicum.entity.compilation;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.LogCompilation;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventMapper;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public static CompilationDto toDto(Compilation entity,
                                       Map<Long, Long> eventHits,
                                       Map<Long, Long> eventRequestsCount) {
        return CompilationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .pinned(entity.getPinned())
                .events(entity.getEvents().stream()
                        .map(event -> EventMapper.toEventShortDto(
                                event,
                                eventRequestsCount.getOrDefault(event.getId(), 0L),
                                eventHits.getOrDefault(event.getId(), 0L)
                        ))
                        .collect(Collectors.toSet()))
                .build();
    }

    public static LogCompilation toLog(CompilationDto compilationDto) {
        return LogCompilation.builder()
                .id(compilationDto.getId())
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned())
                .events(compilationDto.getEvents().stream()
                        .map(EventMapper::toLogEventShort)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        Compilation entity = new Compilation();

        Optional.ofNullable(dto.getPinned()).ifPresent(entity::setPinned);

        if (dto.getEvents() != null) {
            entity.setEvents(events);
        }

        entity.setTitle(dto.getTitle());

        return entity;
    }
}
