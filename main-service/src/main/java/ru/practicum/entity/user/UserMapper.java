package ru.practicum.entity.user;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

@UtilityClass
public class UserMapper {

    static User toUser(final NewUserRequest newUserRequest) {

        return User.builder()
                .mail(newUserRequest.getEmail())
                .name(newUserRequest.getName())
                .rank(CommentsRank.NOVICE)
                .build();
    }

    static UserDto toUserDto(final User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getMail())
                .name(user.getName())
                .build();
    }

    static List<UserDto> toUserDtos(final List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public static UserShortDto toShortDto(final User user) {

        return UserShortDto.builder()
                .name(user.getName())
                .id(user.getId())
                .build();
    }
}
