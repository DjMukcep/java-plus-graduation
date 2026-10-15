package ru.practicum.entity.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;
import ru.practicum.entity.comment.CommentMapper;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor()
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
    public void checkUserExist(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id: " + userId + " does not exist");
        }
    }

    @Override
    @Transactional
    public UserCommentAdminDto updateUserBan(Long userId, LocalDateTime banDate) {
        User user = findById(userId);
        return banDate != null ? banUser(user, banDate) : unbanUser(user);
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
