package ru.practicum.entity.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.LogCompilation;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;

import ru.practicum.entity.category.Category;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.user.User;

import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;


class CompilationMapperTest {

    private Event event;
    private Compilation compilation;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        User initiator = User.builder()
                .id(1L)
                .name("Roman")
                .build();

        event = Event.builder()
                .id(1L)
                .title("Title")
                .annotation("Annotation")
                .category(category)
                .initiator(initiator)
                .eventDate(LocalDateTime.now())
                .paid(false)
                .build();

        compilation = new Compilation();
        compilation.setId(10L);
        compilation.setTitle("Compilation");
        compilation.setPinned(true);
        compilation.setEvents(Set.of(event));
    }

    @Test
    void toDto_shouldMapCompilation() {

        Compilation compilation = new Compilation();
        compilation.setId(10L);
        compilation.setTitle("Compilation");
        compilation.setPinned(true);
        compilation.setEvents(Set.of(event));

        Map<Long, Long> hits = Map.of(1L, 15L);
        Map<Long, Long> requests = Map.of(1L, 7L);

        CompilationDto dto = CompilationMapper.toDto(compilation, hits, requests);

        assertEquals(10L, dto.getId());
        assertEquals("Compilation", dto.getTitle());
        assertTrue(dto.getPinned());

        assertEquals(1, dto.getEvents().size());

        EventShortDto eventDto = dto.getEvents().iterator().next();
        assertEquals(15L, eventDto.getViews());
        assertEquals(7L, eventDto.getConfirmedRequests());
    }

    @Test
    void toDto_shouldUseDefaultValuesWhenMapsAreEmpty() {

        compilation.setEvents(Set.of(event));

        CompilationDto dto = CompilationMapper.toDto(
                compilation,
                Map.of(),
                Map.of()
        );

        EventShortDto eventDto = dto.getEvents().iterator().next();

        assertEquals(0L, eventDto.getViews());
        assertEquals(0L, eventDto.getConfirmedRequests());
    }

    @Test
    void toLog_shouldMapCompilationDto() {
        EventShortDto event = EventShortDto.builder()
                .id(1L)
                .build();

        CompilationDto dto = CompilationDto.builder()
                .id(5L)
                .title("Test")
                .pinned(false)
                .events(Set.of(event))
                .build();

        LogCompilation log = CompilationMapper.toLog(dto);

        assertEquals(5L, log.getId());
        assertEquals("Test", log.getTitle());
        assertFalse(log.getPinned());

        assertEquals(1, log.getEvents().size());
        assertEquals(1L, log.getEvents().iterator().next().getId());
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Event event = Event.builder()
                .id(1L)
                .build();

        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Compilation");
        dto.setPinned(true);
        dto.setEvents(Set.of(1L));

        Compilation entity = CompilationMapper.toEntity(dto, Set.of(event));

        assertEquals("Compilation", entity.getTitle());
        assertTrue(entity.getPinned());
        assertEquals(Set.of(event), entity.getEvents());
    }

    @Test
    void toEntity_shouldHandleNullFields() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Compilation");

        Compilation entity = CompilationMapper.toEntity(dto, Set.of());

        assertEquals("Compilation", entity.getTitle());
        assertFalse(entity.getPinned());
        assertTrue(entity.getEvents().isEmpty());
    }

}