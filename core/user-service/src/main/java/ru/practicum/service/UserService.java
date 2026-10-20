package ru.practicum.service;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;
import ru.practicum.entity.User;


import java.time.LocalDateTime;
import java.util.List;

public interface UserService {

    UserDto saveUser(NewUserRequest userRequest);

    List<UserDto> getUsers(UserParamDto userParamDto);

    void deleteUserById(Long userId);

    User findById(Long userId);

    boolean checkUserExist(Long userId);

    UserCommentAdminDto updateUserBan(Long userId, LocalDateTime banDate);

    User updateUserCommentsCount(Long userId, Integer commentsCount);

    User setWarningToUser(Long userId);
}
