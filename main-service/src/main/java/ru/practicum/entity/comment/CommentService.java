package ru.practicum.entity.comment;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(NewCommentDto newCommentDto, Long userId);

    CommentDto updateComment(UpdateCommentDto updateCommentDto);

    void deleteComment(Long commentId, Long userId);

    List<CommentDto> getComments(Long userId);

    List<CommentDto> getEventComments(Long eventId);

    CommentDto getComment(Long commentId);

    List<CommentDto> searchComments(String text, Pageable pageable);

    UserCommentAdminDto giveWarning(Long commentId);

    Comment getById(Long id);

    void adminDelete(Long commentId);
}
