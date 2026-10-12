package ru.practicum.entity.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void saveUser_shouldSaveUser() {
        NewUserRequest request = NewUserRequest.builder()
                .email("roman@mail.com")
                .name("Roman")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .mail("roman@mail.com")
                .name("Roman")
                .build();

        when(userRepository.existsByMail("roman@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.saveUser(request);

        assertEquals(1L, result.getId());
        assertEquals("roman@mail.com", result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void saveUser_shouldThrowConflictException_whenEmailExists() {
        NewUserRequest request = NewUserRequest.builder()
                .email("roman@mail.com")
                .name("Roman")
                .build();

        when(userRepository.existsByMail("roman@mail.com")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> userService.saveUser(request)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUsers_shouldReturnUsersByIds() {
        UserParamDto params = new UserParamDto();
        params.setIds(Set.of(1L, 2L));

        List<User> users = List.of(
                User.builder().id(1L).mail("a@mail.com").name("A").build(),
                User.builder().id(2L).mail("b@mail.com").name("B").build()
        );

        when(userRepository.findAllByIdIn(argThat(ids ->
                ids.size() == 2 && ids.containsAll(List.of(1L, 2L)))))
                .thenReturn(users);

        List<UserDto> result = userService.getUsers(params);

        assertEquals(2, result.size());

        verify(userRepository).findAllByIdIn(argThat(ids ->
                ids.size() == 2 && ids.containsAll(List.of(1L, 2L))));
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getUsers_shouldReturnPagedUsers() {
        UserParamDto params = new UserParamDto();
        params.setFrom(0);
        params.setSize(10);

        List<User> users = List.of(
                User.builder()
                        .id(1L)
                        .mail("roman@mail.com")
                        .name("Roman")
                        .build()
        );

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(users));

        List<UserDto> result = userService.getUsers(params);

        assertEquals(1, result.size());

        verify(userRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void deleteUserById_shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserById_shouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> userService.deleteUserById(1L)
        );

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateUserBan_whenUserNotBannedAndBanDateNotNull_thenBanUser() {
        LocalDateTime banDate = LocalDateTime.now().plusMonths(3);

        var testUser = new User();
        testUser.setId(1L);
        testUser.setCommentsCount(0);
        testUser.setRank(CommentsRank.NOVICE);
        testUser.setBannedUntil(null);
        testUser.setAdminWarnings(0);

        Mockito.when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        UserCommentAdminDto result = userService.updateUserBan(testUser.getId(), banDate);

        assertEquals(FORMATTER.format(banDate), result.getBannedUntil());
    }

    @Test
    void updateUserBan_whenUserBannedAndBanDateIsNull_thenUnbanUser() {
        LocalDateTime banDate = LocalDateTime.now().plusMonths(3);
        var testUser = new User();
        testUser.setId(1L);
        testUser.setCommentsCount(0);
        testUser.setRank(CommentsRank.NOVICE);
        testUser.setBannedUntil(banDate);
        testUser.setAdminWarnings(0);
        Mockito.when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        UserCommentAdminDto result = userService.updateUserBan(testUser.getId(), null);

        assertNull(result.getBannedUntil());
    }

    @Test
    void updateUserBan_whenUserAlreadyBannedAndBanDateNotNull_thenThrowConflictException() {
        String expectedMessage = "User already banned";
        var testUser = new User();
        testUser.setId(1L);
        testUser.setCommentsCount(0);
        testUser.setRank(CommentsRank.NOVICE);
        testUser.setBannedUntil(LocalDateTime.now().plusMonths(7));
        testUser.setAdminWarnings(0);
        LocalDateTime banDate = LocalDateTime.now().plusMonths(3);

        Mockito.when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUserBan(testUser.getId(), banDate));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void updateUserBan_whenUserNotBannedAndBanDateIsNull_thenThrowConflictException() {
        String expectedMessage = "User not banned";
        var testUser = new User();
        testUser.setId(1L);
        testUser.setCommentsCount(0);
        testUser.setRank(CommentsRank.NOVICE);
        testUser.setBannedUntil(null);
        testUser.setAdminWarnings(0);
        Mockito.when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUserBan(testUser.getId(), null));

        assertEquals(expectedMessage, exception.getMessage());
    }
}