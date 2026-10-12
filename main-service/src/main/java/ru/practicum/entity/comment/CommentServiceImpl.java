package ru.practicum.entity.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.CommentsRank;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;


    @Override
    @Transactional
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId) {
        User user = userService.findById(userId);
        banCheck(user);
        Event event = eventService.findEventByIdAndState(newCommentDto.getEventId(), EventState.PUBLISHED);

        user.setCommentsCount(user.getCommentsCount() + 1);
        setUserRank(user);

        Comment comment = commentRepository.save(CommentMapper.toComment(newCommentDto, user, event));
        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        log.info("Новый комментарий на событие: {}", CommentMapper.toLogComment(commentDto));

        return commentDto;
    }

    @Override
    @Transactional
    public CommentDto updateComment(UpdateCommentDto updateCommentDto) {
        Comment comment = getById(updateCommentDto.getCommentId());
        User user = userService.findById(updateCommentDto.getUserId());

        checkAuthor(comment, updateCommentDto.getUserId());
        banCheck(user);

        comment.setText(updateCommentDto.getText());
        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        log.info("Обновлен комментарий на событие: {}", CommentMapper.toLogComment(commentDto));

        return commentDto;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        User author = comment.getAuthor();
        checkAuthor(comment, userId);

        commentRepository.delete(comment);

        author.setCommentsCount(author.getCommentsCount() - 1);
        setUserRank(author);

        log.info("Комментарий id: {} удален пользователем id: {}", commentId, userId);
    }

    @Override
    public List<CommentDto> getComments(Long userId) {
        return CommentMapper.toCommentDto(commentRepository.findAllByAuthorId(userId));
    }

    @Override
    @Transactional
    public UserCommentAdminDto giveWarning(Long commentId) {
        Comment comment = getById(commentId);
        User author = comment.getAuthor();

        giveWarningForComment(comment);

        return author.isWarningsLimitExceeded() ? banToWarningLimitExceeded(author) : incrementWarningCount(author);
    }

    @Override
    @Transactional
    public void adminDelete(Long commentId) {
        Comment comment = getById(commentId);
        User author = comment.getAuthor();

        CommentDto dto = CommentMapper.toCommentDto(comment);
        author.setCommentsCount(author.getCommentsCount() - 1);
        setUserRank(author);

        commentRepository.delete(comment);

        log.info("Комментарий удален администратором: {}", CommentMapper.toLogComment(dto));
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId) {
        if (!eventService.isEventExists(eventId)) {
            throw new NotFoundException(String.format("Event id: %d not found.", eventId));
        }

        return CommentMapper.toCommentDto(commentRepository.findAllByEventId(eventId));
    }

    @Override
    public CommentDto getComment(Long commentId) {
        return CommentMapper.toCommentDto(getById(commentId));
    }

    @Override
    public List<CommentDto> searchComments(String text, Pageable pageable) {
        List<Comment> comments = commentRepository.findAllByTextContainsIgnoreCase(text, pageable);

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        return CommentMapper.toCommentDto(comments);
    }

    @Override
    public Comment getById(Long id) {
        return commentRepository.findWithRelationsById(id).orElseThrow(
                () -> new NotFoundException("Comment not found with id: " + id)
        );
    }

    private void banCheck(User user) {
        LocalDateTime bannedUntil = user.getBannedUntil();
        if (bannedUntil != null && bannedUntil.isBefore(LocalDateTime.now())) {
            user.setBannedUntil(null);
        }

        if (user.isBanned()) {
            throw new ConflictException("You have been banned!");
        }
    }

    private void checkAuthor(Comment comment, Long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("It's not your own comment!");
        }
    }

    private void setUserRank(User user) {
        int commentsCount = user.getCommentsCount();

        if (commentsCount == 1 && user.getRank() == CommentsRank.REGULAR) {
            user.setRank(CommentsRank.NOVICE);
        }

        if (commentsCount == 2) {
            user.setRank(CommentsRank.REGULAR);
        }

        if (commentsCount > 2 && user.getRank() == CommentsRank.REGULAR) {
            user.setRank(CommentsRank.VETERAN);
        }
    }

    private void giveWarningForComment(Comment comment) {
        User author = comment.getAuthor();
        if (comment.isAdminWarning()) {
            throw new ConflictException(
                    String.format("User with id %s already received warning for comment with id %s", author.getId(), comment.getId())
            );
        }
        comment.setAdminWarning(true);
    }

    private UserCommentAdminDto incrementWarningCount(User author) {
        log.info("Пользователь получил предупреждение: {}", author);
        author.setAdminWarnings(author.getAdminWarnings() + 1);
        return CommentMapper.toUserCommentAdminDto(author);
    }

    private UserCommentAdminDto banToWarningLimitExceeded(User author) {
        banCheck(author);
        log.info("Пользователь заблокирован из-за превышения лимита предупреждений: {}", author);
        author.setAdminWarnings(0);
        author.setBannedUntil(LocalDateTime.now().plusMonths(6));
        return CommentMapper.toUserCommentAdminDto(author);
    }
}
