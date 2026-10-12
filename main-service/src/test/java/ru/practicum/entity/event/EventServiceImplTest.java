package ru.practicum.entity.event;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.StatClient;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.category.CategoryService;
import ru.practicum.entity.request.RequestService;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    @Mock
    private StatClient statClient;

    @Mock
    private RequestService requestService;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void getEventsByAdmin_shouldReturnFilteredEvents() {
        EventAdminParamDto params = EventAdminParamDto.builder()
                .users(List.of(1L))
                .states(List.of("PUBLISHED"))
                .from(0)
                .size(10)
                .build();

        Category category = Category.builder().id(1L).name("cat").build();
        User initiator = User.builder().id(1L).name("user").build();

        Event event = Event.builder()
                .id(1L)
                .state(EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new Location(1.1f,1.1f))
                .category(category)
                .initiator(initiator)
                .build();

        when(eventRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(statClient.getViewStats(any())).thenReturn(Collections.emptyList());

        List<EventFullDto> result = eventService.getEventsByAdmin(params);

        assertEquals(1, result.size());
        verify(statClient).getViewStats(any());
    }

    @Test
    void updateEventByAdmin_publishEvent_shouldSetPublished() {
        Category category = Category.builder().id(1L).name("cat").build();
        User initiator = User.builder().id(1L).name("user").build();

        Event event = Event.builder()
                .id(1L)
                .state(EventState.PENDING)
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .location(new Location(1.1f,1.1f))
                .category(category)
                .initiator(initiator)
                .build();

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        when(eventRepository.findWithRelationsById(1L)).thenReturn(Optional.of(event));
        when(statClient.getViewStats(any())).thenReturn(Collections.emptyList());

        EventFullDto result = eventService.updateEventByAdmin(1L, request);

        assertEquals("PUBLISHED", result.getState());
        assertNotNull(result.getPublishedOn());
    }

    @Test
    void updateEventByAdmin_publishEvent_wrongState_shouldThrowConflict() {
        Category category = Category.builder().id(1L).name("cat").build();
        User initiator = User.builder().id(1L).name("user").build();

        Event event = Event.builder()
                .id(1L)
                .state(EventState.PUBLISHED)
                .eventDate(LocalDateTime.now().plusDays(1))
                .category(category)
                .initiator(initiator)
                .build();

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        when(eventRepository.findWithRelationsById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> eventService.updateEventByAdmin(1L, request));
    }

    @Test
    void getPublishedEventById_notFound_shouldThrowNotFoundException() {
        when(eventRepository.findByIdAndState(999L, EventState.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> eventService.getPublishedEventById(999L, null));
    }

    @Test
    void create_shouldReturnCreatedEvent() {
        User user = User.builder().id(1L).name("user").build();
        CategoryDto categoryDto = CategoryDto.builder().id(1L).name("cat").build();

        String futureDate = LocalDateTime.now().plusDays(1)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        NewEventDto newEvent = NewEventDto.builder()
                .annotation("annotation")
                .category(1L)
                .description("description")
                .eventDate(futureDate)
                .title("title")
                .location(Location.builder().lat(1.0f).lon(1.0f).build())
                .build();

        when(userService.findById(1L)).thenReturn(user);
        when(categoryService.findById(1L)).thenReturn(categoryDto);
        when(eventRepository.save(any())).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EventFullDto result = eventService.create(1L, newEvent);

        assertEquals("title", result.getTitle());
        assertEquals("PENDING", result.getState());
        verify(eventRepository).save(any());
    }
}