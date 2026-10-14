package ru.practicum.controller.private_api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.entity.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class CommentPrivateController {

    private final CommentService commentService;

    @PostMapping("/{userId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable @Positive Long userId) {
        return commentService.createComment(newCommentDto, userId);
    }

    @PatchMapping("/{userId}/comments/{commentId}")
    public CommentDto updateComment(@RequestBody @Valid UpdateCommentDto updateCommentDto,
                                    @PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId) {
        updateCommentDto.setCommentId(commentId);
        updateCommentDto.setUserId(userId);

        return commentService.updateComment(updateCommentDto);
    }

    @DeleteMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long commentId,
                              @PathVariable @Positive Long userId) {
        commentService.deleteComment(commentId, userId);
    }

    @GetMapping("/{userId}/comments")
    public List<CommentDto> getComments(@PathVariable @Positive Long userId) {
        return commentService.getComments(userId);
    }
}
