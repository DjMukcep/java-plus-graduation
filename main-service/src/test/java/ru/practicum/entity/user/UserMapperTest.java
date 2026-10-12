package ru.practicum.entity.user;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUser_shouldMapRequestToUser() {
        NewUserRequest request = NewUserRequest.builder()
                .email("roman@mail.com")
                .name("Roman")
                .build();

        User user = UserMapper.toUser(request);

        assertEquals("roman@mail.com", user.getMail());
        assertEquals("Roman", user.getName());
        assertNull(user.getId());
    }

    @Test
    void toUserDto_shouldMapUserToDto() {
        User user = User.builder()
                .id(1L)
                .mail("roman@mail.com")
                .name("Roman")
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("roman@mail.com", dto.getEmail());
        assertEquals("Roman", dto.getName());
    }

    @Test
    void toUserDtos_shouldMapUsersList() {
        List<User> users = List.of(
                User.builder()
                        .id(1L)
                        .mail("roman@mail.com")
                        .name("Roman")
                        .build(),
                User.builder()
                        .id(2L)
                        .mail("ivan@mail.com")
                        .name("Ivan")
                        .build()
        );

        List<UserDto> result = UserMapper.toUserDtos(users);

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("roman@mail.com", result.get(0).getEmail());

        assertEquals(2L, result.get(1).getId());
        assertEquals("ivan@mail.com", result.get(1).getEmail());
    }
}