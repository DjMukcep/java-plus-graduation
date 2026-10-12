package ru.practicum.entity.compilation;

import com.querydsl.core.types.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationsParamDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.Location;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.User;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventService eventService;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private Event event;
    private Compilation compilation;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .name("Roman")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        event = Event.builder()
                .id(1L)
                .annotation("annotation")
                .title("Event")
                .category(category)
                .initiator(user)
                .eventDate(LocalDateTime.of(2026, 1, 1, 12, 0))
                .createdOn(LocalDateTime.of(2025, 1, 1, 12, 0))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .location(new Location(10f, 20f))
                .build();

        compilation = Compilation.builder()
                .id(1L)
                .title("Compilation")
                .pinned(true)
                .events(Set.of(event))
                .build();
    }

    @Test
    void getAll_shouldReturnCompilations() {
        CompilationsParamDto params = CompilationsParamDto.builder()
                .from(0)
                .size(10)
                .build();

        when(compilationRepository.findAll(Mockito.<Pageable>any()))
                .thenReturn(new PageImpl<>(List.of(compilation)));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of(1L, 15L));

        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of(1L, 7L));

        List<CompilationDto> result = compilationService.getAll(params);

        assertEquals(1, result.size());

        CompilationDto dto = result.getFirst();

        assertEquals(compilation.getId(), dto.getId());
        assertEquals(compilation.getTitle(), dto.getTitle());
        assertTrue(dto.getPinned());

        EventShortDto shortDto = dto.getEvents().iterator().next();

        assertEquals(15L, shortDto.getViews());
        assertEquals(7L, shortDto.getConfirmedRequests());

        verify(compilationRepository).findAll(Mockito.<Pageable>any());
        verify(eventService).getViewsMap(anyList());
        verify(eventService).getEventsRequests(anyList());
    }

    @Test
    void getAll_shouldReturnPinnedCompilations() {
        CompilationsParamDto params = CompilationsParamDto.builder()
                .from(0)
                .size(10)
                .pinned(true)
                .build();

        when(compilationRepository.findAll(
                Mockito.<Predicate>any(),
                Mockito.<Pageable>any()))
                .thenReturn(new PageImpl<>(List.of(compilation)));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of());

        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of());

        List<CompilationDto> result = compilationService.getAll(params);

        assertEquals(1, result.size());

        verify(compilationRepository)
                .findAll(
                        Mockito.<Predicate>any(),
                        Mockito.<Pageable>any()
                );

        verify(compilationRepository, never())
                .findAll(Mockito.<Pageable>any());
    }

    @Test
    void getAll_shouldReturnEmptyList() {
        CompilationsParamDto params = CompilationsParamDto.builder()
                .from(0)
                .size(10)
                .build();

        when(compilationRepository.findAll(Mockito.<Pageable>any()))
                .thenReturn(Page.empty());

        List<CompilationDto> result = compilationService.getAll(params);

        assertTrue(result.isEmpty());

        verifyNoInteractions(eventService);
    }

    @Test
    void getById_shouldReturnCompilation() {
        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.of(compilation));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of(1L, 20L));

        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of(1L, 3L));

        CompilationDto dto = compilationService.getById(1L);

        assertEquals(compilation.getId(), dto.getId());

        EventShortDto shortDto = dto.getEvents().iterator().next();

        assertEquals(20L, shortDto.getViews());
        assertEquals(3L, shortDto.getConfirmedRequests());

        verify(compilationRepository).findWithEventsById(1L);
    }

    @Test
    void getById_shouldThrowNotFound() {
        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> compilationService.getById(1L));

        verifyNoInteractions(eventService);
    }

    @Test
    void create_shouldSaveCompilation() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Compilation");
        dto.setEvents(Set.of(1L, 2L));

        Event event1 = Event.builder()
                .id(1L)
                .initiator(User.builder()
                        .id(1L)
                        .name("Roman")
                        .mail("@mail.ru")
                        .build())
                .category(Category.builder()
                        .id(1L)
                        .name("Category1")
                        .build())
                .eventDate(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
        Event event2 = Event.builder()
                .id(2L)
                .initiator(User.builder()
                        .id(2L)
                        .name("Oleg")
                        .mail("@yandex.ru")
                        .build())
                .category(Category.builder()
                        .id(2L)
                        .name("Category2")
                        .build())
                .eventDate(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();

        when(compilationRepository.existsByTitle("Compilation"))
                .thenReturn(false);

        when(eventService.getByIds(dto.getEvents()))
                .thenReturn(List.of(event1, event2));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of(1L, 10L, 2L, 20L));

        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of(1L, 5L, 2L, 7L));

        when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenAnswer(invocation -> {
                    Compilation c = invocation.getArgument(0);
                    c.setId(100L);
                    return c;
                });

        CompilationDto result = compilationService.create(dto);

        assertEquals(100L, result.getId());
        assertEquals("Compilation", result.getTitle());

        assertEquals(2, result.getEvents().size());

        verify(compilationRepository).existsByTitle("Compilation");
        verify(eventService).getByIds(dto.getEvents());
        verify(compilationRepository).save(Mockito.<Compilation>any());
    }

    @Test
    void create_shouldThrowConflictWhenTitleExists() {

        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Compilation");

        when(compilationRepository.existsByTitle("Compilation"))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> compilationService.create(dto));

        verify(compilationRepository, never()).save(any());
        verifyNoInteractions(eventService);
    }

    @Test
    void delete_shouldDeleteCompilation() {

        Compilation compilation = new Compilation();
        compilation.setId(1L);

        when(compilationRepository.findById(1L))
                .thenReturn(Optional.of(compilation));

        compilationService.delete(1L);

        verify(compilationRepository).delete(compilation);
    }

    @Test
    void delete_shouldThrowNotFound() {

        when(compilationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> compilationService.delete(1L));

        verify(compilationRepository, never()).delete(any());
    }

    @Test
    void update_shouldChangeTitleAndPinned() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("New title");
        request.setPinned(false);

        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.of(compilation));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of(1L, 10L));

        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of(1L, 5L));

        CompilationDto result = compilationService.update(1L, request);

        assertEquals("New title", result.getTitle());
        assertFalse(result.getPinned());

        verify(compilationRepository).findWithEventsById(1L);
    }

    @Test
    void update_shouldChangeOnlyPinned() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setPinned(false);

        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.of(compilation));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of());
        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of());

        CompilationDto result = compilationService.update(1L, request);

        assertFalse(result.getPinned());
        assertEquals("Compilation", result.getTitle());
    }

    @Test
    void update_shouldUpdateEvents() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setEvents(Set.of(2L, 3L));

        Category category = Category.builder().id(1L).name("Category").build();
        User user = User.builder().id(1L).name("User").build();

        Event event2 = Event.builder()
                .id(2L)
                .category(category)
                .initiator(user)
                .annotation("a")
                .title("t")
                .eventDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        Event event3 = Event.builder()
                .id(3L)
                .category(category)
                .initiator(user)
                .annotation("a")
                .title("t")
                .eventDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.of(compilation));

        when(eventService.getByIds(request.getEvents()))
                .thenReturn(List.of(event2, event3));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of());

        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of());

        CompilationDto result = compilationService.update(1L, request);

        assertEquals(2, result.getEvents().size());

        verify(eventService).getByIds(request.getEvents());
    }

    @Test
    void update_shouldNotCallEventServiceWhenEventsUnchanged() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Compilation"); // same title
        request.setPinned(true);
        request.setEvents(compilation.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toSet()));

        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.of(compilation));

        when(eventService.getViewsMap(anyList()))
                .thenReturn(Map.of(1L, 10L));

        when(eventService.getEventsRequests(anyList()))
                .thenReturn(Map.of(1L, 5L));

        compilationService.update(1L, request);

        verify(eventService, never()).getByIds(any());
    }

    @Test
    void update_shouldThrowNotFound() {

        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> compilationService.update(1L, new UpdateCompilationRequest()));

        verifyNoInteractions(eventService);
    }

    @Test
    void update_shouldThrowConflictWhenTitleExists() {

        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("New title");

        Compilation other = Compilation.builder()
                .id(2L)
                .title("New title")
                .build();

        when(compilationRepository.findWithEventsById(1L))
                .thenReturn(Optional.of(compilation));

        when(compilationRepository.existsByTitle("New title"))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> compilationService.update(1L, request));
    }
}