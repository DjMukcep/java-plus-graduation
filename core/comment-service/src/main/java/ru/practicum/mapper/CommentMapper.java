package ru.practicum.mapper;

import lombok.experimental.UtilityClass;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.LogComment;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Comment;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@UtilityClass
public class CommentMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Comment toComment(NewCommentDto comment, Long authorId, Long eventId) {
        return Comment.builder()
                .text(comment.getText())
                .authorId(authorId)
                .eventId(eventId)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment, String authorName) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEventId())
                .authorName(authorName)
                .text(comment.getText())
                .created(comment.getCreated().format(formatter))
                .updated(comment.getUpdated() != null ?
                        comment.getUpdated().format(formatter) : null)
                .build();
    }

    public static List<CommentDto> toCommentDto(List<Comment> comments, Map<Long, UserDto> authors) {
        return comments.stream()
                .map(comment -> toCommentDto(comment,authors.get(comment.getAuthorId()).getName()))
                .toList();
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
