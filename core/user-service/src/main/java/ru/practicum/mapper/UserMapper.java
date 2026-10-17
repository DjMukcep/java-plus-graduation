package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.entity.CommentsRank;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.User;


import java.util.List;

@UtilityClass
public class UserMapper {

    public static User toUser(final NewUserRequest newUserRequest) {

        return User.builder()
                .mail(newUserRequest.getEmail())
                .name(newUserRequest.getName())
                .rank(CommentsRank.NOVICE)
                .build();
    }

    public static UserDto toUserDto(final User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getMail())
                .name(user.getName())
                .build();
    }

    public static List<UserDto> toUserDtos(final List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }
}
