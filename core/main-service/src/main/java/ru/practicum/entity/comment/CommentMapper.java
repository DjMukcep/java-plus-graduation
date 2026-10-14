package ru.practicum.entity.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.LogComment;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.user.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

@UtilityClass
public class CommentMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Comment toComment(NewCommentDto comment, User author, Event event) {
        return Comment.builder()
                .text(comment.getText())
                .author(author)
                .event(event)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorId(comment.getAuthor().getId())
                .eventId(comment.getEvent().getId())
                .authorName(comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated().format(formatter))
                .updated(comment.getUpdated() != null ?
                        comment.getUpdated().format(formatter) : null)
                .build();
    }

    public static List<CommentDto> toCommentDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    public static UserCommentAdminDto toUserCommentAdminDto(User user) {
        return UserCommentAdminDto.builder()
                .authorId(user.getId())
                .authorName(user.getName())
                .adminWarnCount(user.getAdminWarnings())
                .bannedUntil(user.getBannedUntil() != null ? user.getBannedUntil().format(formatter) : null)
                .build();
    }

    public static LogComment toLogComment(CommentDto comment) {
        return LogComment.builder()
                .id(comment.getId())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEventId())
                .authorName(comment.getAuthorName())
                .text(stringBuilder(comment.getText()))
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }

    private static String stringBuilder(String string) {
        if (string == null) {
            return null;
        }
        return string.length() > 10 ? string.substring(0, 10) + "..." : string;
    }
}
