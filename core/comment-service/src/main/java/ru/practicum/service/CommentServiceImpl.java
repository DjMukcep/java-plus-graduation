package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;

import ru.practicum.entity.Comment;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.internal.event.EventClient;
import ru.practicum.internal.user.UserClient;
import ru.practicum.repository.CommentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;


    @Override
    @Transactional
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId) {
        UserCommentAdminDto user = userClient.getUserCommentStats(userId);
        banCheck(user);
        Long eventId = newCommentDto.getEventId();

        if (!eventClient.checkEventExistsAndPublished(eventId)) {
            throw new NotFoundException("Event not found eventId: " + newCommentDto.getEventId());
        }

        user = userClient.setCommentsCount(user.getAuthorId(),user.getCommentCount() + 1);
        Comment comment = commentRepository.save(CommentMapper.toComment(newCommentDto, user.getAuthorId(), eventId));
        CommentDto commentDto = CommentMapper.toCommentDto(comment,user.getAuthorName());

        log.info("Новый комментарий на событие: {}", CommentMapper.toLogComment(commentDto));

        return commentDto;
    }

    @Override
    @Transactional
    public CommentDto updateComment(UpdateCommentDto updateCommentDto) {
        Comment comment = getById(updateCommentDto.getCommentId());
        UserCommentAdminDto user = userClient.getUserCommentStats(updateCommentDto.getUserId());

        checkAuthor(comment, updateCommentDto.getUserId());
        banCheck(user);

        comment.setText(updateCommentDto.getText());
        CommentDto commentDto = CommentMapper.toCommentDto(comment,user.getAuthorName());
        log.info("Обновлен комментарий на событие: {}", CommentMapper.toLogComment(commentDto));

        return commentDto;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        UserCommentAdminDto author = userClient.getUserCommentStats(comment.getAuthorId());
        checkAuthor(comment, userId);

        commentRepository.delete(comment);

        userClient.setCommentsCount(author.getAuthorId(),author.getCommentCount() - 1);

        log.info("Комментарий id: {} удален пользователем id: {}", commentId, userId);
    }

    @Override
    public List<CommentDto> getComments(Long userId) {
        List<Comment> comments = commentRepository.findAllByAuthorId(userId);
        Map<Long, UserDto> users = getUsers(comments);

        return CommentMapper.toCommentDto(comments, users);
    }

    @Override
    @Transactional
    public UserCommentAdminDto giveWarning(Long commentId) {
        Comment comment = getById(commentId);
        Long authorId = comment.getAuthorId();

        giveWarningForComment(comment);

        return userClient.setWarningToUser(authorId);
    }

    @Override
    @Transactional
    public void adminDelete(Long commentId) {
        Comment comment = getById(commentId);
        UserCommentAdminDto author = userClient.getUserCommentStats(comment.getAuthorId());

        CommentDto dto = CommentMapper.toCommentDto(comment,author.getAuthorName());
        userClient.setCommentsCount(author.getAuthorId(),author.getCommentCount() - 1);

        commentRepository.delete(comment);

        log.info("Комментарий удален администратором: {}", CommentMapper.toLogComment(dto));
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId) {
        if (!eventClient.checkEventExists(eventId)) {
            throw new NotFoundException(String.format("Event id: %d not found.", eventId));
        }
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        Map<Long, UserDto> users = getUsers(comments);

        return CommentMapper.toCommentDto(comments, users);
    }


    @Override
    public CommentDto getComment(Long commentId) {
        Comment comment = getById(commentId);
        UserCommentAdminDto author = userClient.getUserCommentStats(comment.getAuthorId());
        return CommentMapper.toCommentDto(comment,author.getAuthorName());
    }

    @Override
    public List<CommentDto> searchComments(String text, Pageable pageable) {
        List<Comment> comments = commentRepository.findAllByTextContainsIgnoreCase(text, pageable);
        Map<Long, UserDto> users = getUsers(comments);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        return CommentMapper.toCommentDto(comments, users);
    }

    @Override
    public Comment getById(Long id) {
        return commentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Comment not found with id: " + id)
        );
    }

    private Map<Long, UserDto> getUsers(List<Comment> events) {
        Set<Long> userIds = events.stream()
                .map(Comment::getAuthorId)
                .collect(Collectors.toSet());

        UserParamDto userParamDto = new UserParamDto();
        userParamDto.setIds(userIds);

        userParamDto.setSize(userIds.size());

        return userClient.getUsers(userParamDto).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
    }

    private void banCheck(UserCommentAdminDto user) {
        if (user.isBanned()) {
            throw new ConflictException("You have been banned!");
        }
    }

    private void checkAuthor(Comment comment, Long userId) {
        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("It's not your own comment!");
        }
    }

    private void giveWarningForComment(Comment comment) {
        if (comment.isAdminWarning()) {
            throw new ConflictException(
                    String.format("User with id %s already received warning for comment with id %s",
                            comment.getAuthorId(), comment.getId())
            );
        }
        comment.setAdminWarning(true);
    }
}
