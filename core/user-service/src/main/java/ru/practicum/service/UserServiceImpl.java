package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.CommentsRank;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;
import ru.practicum.entity.User;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto saveUser(NewUserRequest userRequest) {
        if (userRepository.existsByMail(userRequest.getEmail())) {
            throw new ConflictException("User with this email already exists");
        }

        User savedUser = userRepository.save(UserMapper.toUser(userRequest));
        log.info("Новый пользователь {}", savedUser);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(UserParamDto userParamDto) {

        if (userParamDto.getIds() != null && !userParamDto.getIds().isEmpty()) {
            List<Long> ids = userParamDto.getIds().stream().toList();
            return UserMapper.toUserDtos(userRepository.findAllByIdIn(ids));
        }

        if (userParamDto.getSize() == 0) {
            return List.of();
        }

        int page = userParamDto.getFrom() / userParamDto.getSize();
        int pageSize = userParamDto.getSize();
        List<User> users = userRepository.findAll(PageRequest.of(page, pageSize)).getContent();

        return UserMapper.toUserDtos(users);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        checkUserExist(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id: " + userId + " does not exist"));
    }

    @Override
    public boolean checkUserExist(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional
    public UserCommentAdminDto updateUserBan(Long userId, LocalDateTime banDate) {
        User user = findById(userId);
        return banDate != null ? banUser(user, banDate) : unbanUser(user);
    }

    @Override
    @Transactional
    public User updateUserCommentsCount(Long userId, Integer commentsCount) {
        User user = findById(userId);

        if (commentsCount != null && commentsCount >= 0) {
            user.setCommentsCount(commentsCount);
        }
        setUserRank(user);

        return user;
    }

    @Override
    @Transactional
    public User setWarningToUser(Long userId) {
        User author = findById(userId);
        return author.isWarningsLimitExceeded()
                ? banForWarningLimitExceeded(author)
                : incrementWarningCount(author);

    }

    private User incrementWarningCount(User author) {
        log.info("Пользователь получил предупреждение: {}", author);
        author.setAdminWarnings(author.getAdminWarnings() + 1);

        return author;
    }

    private User banForWarningLimitExceeded(User author) {
        log.info("Пользователь заблокирован из-за превышения лимита предупреждений: {}", author);
        author.setAdminWarnings(0);
        author.setBannedUntil(LocalDateTime.now().plusMonths(6));

        return author;
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

    private static UserCommentAdminDto unbanUser(User user) {
        if (!user.isBanned()) {
            throw new ConflictException("User not banned");
        }

        log.info("Пользователь разблокирован:{}", user);
        user.setBannedUntil(null);
        return CommentMapper.toUserCommentAdminDto(user);
    }

    private static UserCommentAdminDto banUser(User user, LocalDateTime banDate) {
        if (user.isBanned()) {
            throw new ConflictException("User already banned");
        }

        log.info("Пользователь получил блокировку до {}: {}", banDate,  user);
        user.setBannedUntil(banDate);
        return CommentMapper.toUserCommentAdminDto(user);
    }
}
