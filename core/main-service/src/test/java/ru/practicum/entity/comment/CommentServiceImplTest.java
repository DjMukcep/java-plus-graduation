package ru.practicum.entity.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.CommentsRank;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserService userService;
    @Mock
    private EventService eventService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private User testUser;
    private Event testEvent;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setCommentsCount(0);
        testUser.setRank(CommentsRank.NOVICE);
        testUser.setBannedUntil(null);
        testUser.setAdminWarnings(0);

        testEvent = new Event();
        testEvent.setId(10L);
        testEvent.setState(EventState.PUBLISHED);

        testComment = new Comment();
        testComment.setId(20L);
        testComment.setAdminWarning(false);
        testComment.setAuthor(testUser);
        testComment.setEvent(testEvent);
        testComment.setCreated(LocalDateTime.now().minusDays(10));
        testComment.setUpdated(null);
    }

    @Test
    void createComment_whenValid_thenSuccessAndChangesRank() {
        testUser.setCommentsCount(1);
        NewCommentDto newCommentDto = new NewCommentDto("Текст", 10L);

        when(userService.findById(1L)).thenReturn(testUser);
        when(eventService.findEventByIdAndState(10L, EventState.PUBLISHED)).thenReturn(testEvent);

        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(100L);
            c.setCreated(LocalDateTime.now());
            return c;
        });

        CommentDto result = commentService.createComment(newCommentDto, 1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Текст", result.getText());
        assertEquals(2, testUser.getCommentsCount());

        assertEquals(CommentsRank.REGULAR, testUser.getRank());
    }

    @Test
    void createComment_whenUserIsBanned_thenThrowsConflictException() {
        // Ставим бан в будущем времени
        testUser.setBannedUntil(LocalDateTime.now().plusDays(1));
        NewCommentDto newCommentDto = new NewCommentDto("Текст", 10L);

        when(userService.findById(1L)).thenReturn(testUser);

        assertThrows(ConflictException.class, () -> commentService.createComment(newCommentDto, 1L));
        verify(commentRepository, Mockito.never()).save(any());
    }

    @Test
    void createComment_whenBanExpired_thenResetsBanAndSuccess() {
        testUser.setBannedUntil(LocalDateTime.now().minusDays(1));
        NewCommentDto newCommentDto = new NewCommentDto("Текст", 10L);

        when(userService.findById(1L)).thenReturn(testUser);
        when(eventService.findEventByIdAndState(10L, EventState.PUBLISHED)).thenReturn(testEvent);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setCreated(LocalDateTime.now());
            return c;
        });

        assertDoesNotThrow(() -> commentService.createComment(newCommentDto, 1L));
        assertNull(testUser.getBannedUntil());
    }

    @Test
    void updateComment_whenValid_thenSuccess() {
        Event fakeEvent = new Event();
        fakeEvent.setId(10L);

        Comment existingComment = new Comment();
        existingComment.setId(100L);
        existingComment.setCreated(LocalDateTime.now());
        existingComment.setUpdated(LocalDateTime.now());
        existingComment.setText("Старый текст");
        existingComment.setAuthor(testUser);
        existingComment.setEvent(fakeEvent);

        UpdateCommentDto updateDto = new UpdateCommentDto();
        updateDto.setCommentId(100L);
        updateDto.setUserId(1L);
        updateDto.setText("Новый текст");

        when(commentRepository.findWithRelationsById(100L)).thenReturn(Optional.of(existingComment));
        when(userService.findById(1L)).thenReturn(testUser);

        CommentDto result = commentService.updateComment(updateDto);

        assertNotNull(result);
        assertEquals("Новый текст", result.getText());
        assertEquals("Новый текст", existingComment.getText());
    }

    @Test
    void updateComment_whenNotAuthor_thenThrowsConflictException() {
        // 1. Создаем автора оригинального комментария (Юзер 1)
        User realAuthor = new User();
        realAuthor.setId(1L);

        Comment existingComment = new Comment();
        existingComment.setId(100L);
        existingComment.setAuthor(realAuthor);

        // 2. Создаем пользователя, который ПЫТАЕТСЯ обновить (Юзер 999)
        User hackerUser = new User();
        hackerUser.setId(999L);
        hackerUser.setBannedUntil(null); // чтобы прошел banCheck

        UpdateCommentDto updateDto = new UpdateCommentDto();
        updateDto.setCommentId(100L);
        updateDto.setUserId(999L);
        updateDto.setText("Новый текст");

        when(commentRepository.findWithRelationsById(100L)).thenReturn(Optional.of(existingComment));
        when(userService.findById(999L)).thenReturn(hackerUser);

        assertThrows(ConflictException.class, () -> commentService.updateComment(updateDto));
    }

    @Test
    void deleteComment_whenValid_thenCallsDelete() {
        Comment existingComment = new Comment();
        existingComment.setId(100L);
        testUser.setCommentsCount(1);
        existingComment.setAuthor(testUser);


        when(commentRepository.findWithRelationsById(100L)).thenReturn(Optional.of(existingComment));

        assertDoesNotThrow(() -> commentService.deleteComment(100L, 1L));

        verify(commentRepository, times(1)).delete(existingComment);
    }

    @Test
    void deleteComment_whenCommentNotFound_thenThrowsNotFoundException() {
        when(commentRepository.findWithRelationsById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.deleteComment(100L, 1L));
    }

    @Test
    void getComments_whenCalled_thenReturnsList() {
        Event fakeEvent = new Event();
        fakeEvent.setId(10L);

        Comment c1 = new Comment();
        c1.setId(100L);
        c1.setAuthor(testUser);
        c1.setEvent(fakeEvent);
        c1.setText("Текст 1");
        c1.setCreated(LocalDateTime.now());

        Comment c2 = new Comment();
        c2.setId(101L);
        c2.setAuthor(testUser);
        c2.setEvent(fakeEvent);
        c2.setText("Текст 2");
        c2.setCreated(LocalDateTime.now());

        when(commentRepository.findAllByAuthorId(1L)).thenReturn(List.of(c1, c2));

        List<CommentDto> result = commentService.getComments(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Текст 1", result.get(0).getText());
        assertEquals("Текст 2", result.get(1).getText());
    }

    @Test
    void incrementWarning_whenUserNotExceededWarningsLimitAndCommentNotReceivedWarning_Count_thenIncreaseWarningsCount() {
        when(commentRepository.findWithRelationsById(20L)).thenReturn(Optional.of(testComment));

        UserCommentAdminDto result = commentService.giveWarning(testComment.getId());

        assertEquals(testUser.getId(), result.getAuthorId());
        assertEquals(1, result.getAdminWarnCount());
        assertTrue(testComment.isAdminWarning());
    }

    @Test
    void incrementWarning_whenCommentAlreadyReceivedWarning_Count_thenThrowConflictException() {
        String expectedMessage = String.format("User with id %s already received warning for comment with id %s",
                testUser.getId(), testComment.getId());

        testComment.setAdminWarning(true);
        when(commentRepository.findWithRelationsById(20L)).thenReturn(Optional.of(testComment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> commentService.giveWarning(testComment.getId()));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void incrementWarning_Count_whenUserExceededWarningsLimit_thenBanUserFor6Months() {
        LocalDateTime expectedBanDate = LocalDateTime.now().plusMonths(6);
        testUser.setAdminWarnings(2);
        when(commentRepository.findWithRelationsById(20L)).thenReturn(Optional.of(testComment));

        UserCommentAdminDto result = commentService.giveWarning(testComment.getId());

        assertEquals(0, result.getAdminWarnCount());
        var banDate = LocalDateTime.parse(result.getBannedUntil(), FORMATTER);
        assertEquals(expectedBanDate.getYear(), banDate.getYear());
        assertEquals(expectedBanDate.getMonth(), banDate.getMonth());
        assertEquals(expectedBanDate.getDayOfYear(), banDate.getDayOfYear());
    }

    @Test
    void incrementWarning_Count_whenUserExceededWarningsLimitAndUserAlreadyBanned_thenThrowConflictException() {
        String expectedMessage = "You have been banned!";
        testUser.setAdminWarnings(2);
        testUser.setBannedUntil(LocalDateTime.now().plusMonths(3));
        when(commentRepository.findWithRelationsById(20L)).thenReturn(Optional.of(testComment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> commentService.giveWarning(testComment.getId()));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void adminDelete_whenValid_thenCallsDelete() {
        when(commentRepository.findWithRelationsById(testComment.getId())).thenReturn(Optional.of(testComment));

        assertDoesNotThrow(() -> commentService.adminDelete(testComment.getId()));

        verify(commentRepository, times(1)).delete(testComment);
    }

    @Test
    void adminDelete_whenCommentNotFound_thenThrowsNotFoundException() {
        when(commentRepository.findWithRelationsById(testComment.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.adminDelete(testComment.getId()));
    }

    @Test
    void getEventComments_WhenCommentsExist_ThenReturnDtoList() {
        Long eventId = 1L;
        when(commentRepository.findAllByEventId(eventId)).thenReturn(List.of(testComment));
        when(eventService.isEventExists(eventId)).thenReturn(true);

        List<CommentDto> result = commentService.getEventComments(eventId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getId(), result.getFirst().getId());
        assertEquals(testComment.getText(), result.getFirst().getText());
        verify(commentRepository, times(1)).findAllByEventId(eventId);
    }

    @Test
    void getEventComments_WhenNoComments_ThenThrowsNotFoundException() {
        Long eventId = 1L;

        assertThrows(NotFoundException.class, () -> commentService.getEventComments(eventId));
        verify(commentRepository, never()).findAllByEventId(eventId);
    }

    // --- Тесты для getComment(Long commentId) ---

    @Test
    void getComment_WhenCommentExists_ThenReturnDto() {
        Long commentId = testComment.getId();
        when(commentRepository.findWithRelationsById(commentId)).thenReturn(Optional.of(testComment));

        CommentDto result = commentService.getComment(commentId);

        assertNotNull(result);
        assertEquals(testComment.getId(), result.getId());
        assertEquals(testComment.getText(), result.getText());
        verify(commentRepository, times(1)).findWithRelationsById(commentId);
    }

    @Test
    void getComment_WhenCommentDoesNotExist_ThenThrowException() {
        Long commentId = 99L;
        when(commentRepository.findWithRelationsById(commentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.getComment(commentId));
        verify(commentRepository, times(1)).findWithRelationsById(commentId);
    }

    // --- Тесты для searchComments(String text) ---

    @Test
    void searchComments_WhenMatchesFound_ThenReturnDtoList() {
        String searchText = "Тест";
        Pageable pageable = PageRequest.of(0, 10);
        when(commentRepository.findAllByTextContainsIgnoreCase(searchText, pageable)).thenReturn(List.of(testComment));

        List<CommentDto> result = commentService.searchComments(searchText, pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getText(), result.getFirst().getText());
        verify(commentRepository, times(1)).findAllByTextContainsIgnoreCase(searchText, pageable);
    }

    @Test
    void searchComments_WhenNoMatches_ThenReturnEmptyList() {
        String searchText = "НичегоНеНайдено";
        Pageable pageable = PageRequest.of(0, 10);
        when(commentRepository.findAllByTextContainsIgnoreCase(searchText,pageable)).thenReturn(Collections.emptyList());

        List<CommentDto> result = commentService.searchComments(searchText, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1)).findAllByTextContainsIgnoreCase(searchText, pageable);
    }
}