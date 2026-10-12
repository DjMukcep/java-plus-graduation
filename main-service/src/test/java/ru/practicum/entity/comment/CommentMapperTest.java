package ru.practicum.entity.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.LogComment;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private User testUser;
    private Event testEvent;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2026, 7, 2, 19, 0, 0);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Иван Иванов");
        testUser.setAdminWarnings(3);
        testUser.setBannedUntil(fixedTime);

        testEvent = new Event();
        testEvent.setId(10L);
    }


    @Test
    void toComment_whenValid_thenMapCorrectly() {
        NewCommentDto newCommentDto = new NewCommentDto("Текст тестового комментария", 10L);

        Comment result = CommentMapper.toComment(newCommentDto, testUser, testEvent);

        assertNotNull(result);
        assertEquals("Текст тестового комментария", result.getText());
        assertEquals(testUser, result.getAuthor());
        assertEquals(testEvent, result.getEvent());
    }

    @Test
    void toCommentDto_whenUpdatedIsNull_thenMapCorrectly() {
        Comment comment = Comment.builder()
                .id(100L)
                .text("Привет")
                .author(testUser)
                .event(testEvent)
                .created(fixedTime)
                .updated(null) // проверяем ветку, когда обновлений не было
                .build();

        CommentDto result = CommentMapper.toCommentDto(comment);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getAuthorId());
        assertEquals(10L, result.getEventId());
        assertEquals("Иван Иванов", result.getAuthorName());
        assertEquals("Привет", result.getText());
        assertEquals("2026-07-02 19:00:00", result.getCreated()); // проверка паттерна даты
        assertNull(result.getUpdated());
    }

    @Test
    void toCommentDto_whenUpdatedIsPresent_thenFormatUpdatedDate() {
        Comment comment = Comment.builder()
                .id(100L)
                .text("Привет")
                .author(testUser)
                .event(testEvent)
                .created(fixedTime)
                .updated(fixedTime.plusHours(2)) // 2026-07-02 21:00:00
                .build();

        CommentDto result = CommentMapper.toCommentDto(comment);

        assertEquals("2026-07-02 21:00:00", result.getUpdated());
    }

    @Test
    void toCommentDto_listMethod_thenMapList() {
        Comment comment = Comment.builder()
                .id(100L).text("Текст").author(testUser).event(testEvent).created(fixedTime)
                .build();

        List<CommentDto> result = CommentMapper.toCommentDto(List.of(comment));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    void toUserCommentAdminDto_whenValid_thenMapCorrectly() {
        UserCommentAdminDto result = CommentMapper.toUserCommentAdminDto(testUser);

        assertNotNull(result);
        assertEquals(1L, result.getAuthorId());
        assertEquals("Иван Иванов", result.getAuthorName());
        assertEquals(3, result.getAdminWarnCount());
        assertEquals("2026-07-02 19:00:00", result.getBannedUntil());
    }

    @Test
    void toLogComment_whenTextIsLong_thenAbbreviateText() {
        CommentDto dto = CommentDto.builder()
                .id(50L).authorId(1L).eventId(10L).authorName("Иван")
                .text("ОченьДлинныйТекстБольшеДесятиСимволов") // 36 символов
                .created("2026-07-02 19:00:00").updated(null)
                .build();

        LogComment result = CommentMapper.toLogComment(dto);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("ОченьДлинн...", result.getText());
    }

    @Test
    void toLogComment_whenTextIsShortOrNull_thenMapWithoutChanges() {
        CommentDto dtoShort = CommentDto.builder().text("Привет").created("2026").build();
        LogComment resultShort = CommentMapper.toLogComment(dtoShort);
        assertEquals("Привет", resultShort.getText());

        CommentDto dtoNull = CommentDto.builder().text(null).created("2026").build();
        LogComment resultNull = CommentMapper.toLogComment(dtoNull);
        assertNull(resultNull.getText());
    }
}