package ru.practicum.entity.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventRepository;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    void saveRequest_shouldSaveRequest() {
        Long userId = 1L;
        Long eventId = 2L;

        User user = User.builder().id(userId).build();

        User initiator = User.builder().id(99L).build();

        Event event = Event.builder()
                .id(eventId)
                .initiator(initiator)
                .state(EventState.PUBLISHED)
                .participantLimit(0)
                .requestModeration(true)
                .build();

        Request savedRequest = Request.builder()
                .id(10L)
                .user(user)
                .event(event)
                .createdAt(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();

        when(requestRepository.existsByUserIdAndEventId(userId, eventId))
                .thenReturn(false);
        when(userService.findById(userId)).thenReturn(user);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        ParticipationRequestDto result =
                requestService.saveRequest(userId, eventId);

        assertEquals(10L, result.getId());

        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void saveRequest_shouldThrowConflict_whenDuplicateRequest() {
        when(requestRepository.existsByUserIdAndEventId(1L, 2L))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> requestService.saveRequest(1L, 2L)
        );

        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    void saveRequest_shouldThrowNotFound_whenEventNotExists() {
        when(requestRepository.existsByUserIdAndEventId(1L, 2L))
                .thenReturn(false);

        when(userService.findById(1L))
                .thenReturn(User.builder().id(1L).build());

        when(eventRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> requestService.saveRequest(1L, 2L)
        );
    }

    @Test
    void saveRequest_shouldThrowConflict_whenUserIsInitiator() {
        User user = User.builder().id(1L).build();

        Event event = Event.builder()
                .id(2L)
                .initiator(user)
                .state(EventState.PUBLISHED)
                .build();

        when(requestRepository.existsByUserIdAndEventId(1L, 2L))
                .thenReturn(false);
        when(userService.findById(1L)).thenReturn(user);
        when(eventRepository.findById(2L))
                .thenReturn(Optional.of(event));

        assertThrows(
                ConflictException.class,
                () -> requestService.saveRequest(1L, 2L)
        );
    }

    @Test
    void saveRequest_shouldThrowConflict_whenEventNotPublished() {
        User user = User.builder().id(1L).build();

        Event event = Event.builder()
                .id(2L)
                .initiator(User.builder().id(99L).build())
                .state(EventState.CANCELED)
                .build();

        when(requestRepository.existsByUserIdAndEventId(1L, 2L))
                .thenReturn(false);
        when(userService.findById(1L)).thenReturn(user);
        when(eventRepository.findById(2L))
                .thenReturn(Optional.of(event));

        assertThrows(
                ConflictException.class,
                () -> requestService.saveRequest(1L, 2L)
        );
    }

    @Test
    void saveRequest_shouldThrowConflict_whenParticipantLimitReached() {
        User user = User.builder().id(1L).build();

        Event event = Event.builder()
                .id(2L)
                .initiator(User.builder().id(99L).build())
                .state(EventState.PUBLISHED)
                .participantLimit(5)
                .build();

        when(requestRepository.existsByUserIdAndEventId(1L, 2L))
                .thenReturn(false);

        when(userService.findById(1L))
                .thenReturn(user);

        when(eventRepository.findById(2L))
                .thenReturn(Optional.of(event));

        when(requestRepository.countByEventIdAndStatus(
                2L,
                RequestStatus.CONFIRMED))
                .thenReturn(5L);

        assertThrows(
                ConflictException.class,
                () -> requestService.saveRequest(1L, 2L)
        );
    }

    @Test
    void cancelRequest_shouldCancelRequest() {
        User user = User.builder().id(1L).build();

        Request request = Request.builder()
                .id(10L)
                .user(user)
                .status(RequestStatus.CONFIRMED)
                .event(Event.builder().id(2L).build())
                .createdAt(LocalDateTime.now())
                .build();

        when(requestRepository.findById(10L))
                .thenReturn(Optional.of(request));

        ParticipationRequestDto result =
                requestService.cancelRequest(1L, 10L);

        assertEquals("CANCELED", result.getStatus());
        assertEquals(RequestStatus.CANCELED, request.getStatus());
    }

    @Test
    void cancelRequest_shouldThrowConflict_whenRequestBelongsToAnotherUser() {
        User owner = User.builder().id(2L).build();

        Request request = Request.builder()
                .id(10L)
                .user(owner)
                .build();

        when(requestRepository.findById(10L))
                .thenReturn(Optional.of(request));

        assertThrows(
                ConflictException.class,
                () -> requestService.cancelRequest(1L, 10L)
        );
    }

    @Test
    void getUserRequests_shouldReturnRequests() {
        Request request = Request.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .event(Event.builder().id(2L).build())
                .createdAt(LocalDateTime.now())
                .status(RequestStatus.CONFIRMED)
                .build();

        when(requestRepository.findByUserId(1L))
                .thenReturn(List.of(request));

        List<ParticipationRequestDto> result =
                requestService.getUserRequests(1L);

        assertEquals(1, result.size());

        verify(userService).checkUserExist(1L);
    }
}