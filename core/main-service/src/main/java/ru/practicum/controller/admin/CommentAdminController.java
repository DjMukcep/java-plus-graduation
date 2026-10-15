package ru.practicum.controller.admin;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.comment.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentService commentService;

    @PatchMapping("/{commentId}")
    public UserCommentAdminDto warnUser(@PathVariable @Positive Long commentId) {
        return commentService.giveWarning(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long commentId) {
        commentService.adminDelete(commentId);
    }
}
