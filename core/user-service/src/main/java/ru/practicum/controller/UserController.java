package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.service.UserService;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto saveUser(@RequestBody @Valid NewUserRequest userRequest) {
        return userService.saveUser(userRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@ModelAttribute @Valid UserParamDto userParamDto) {
        return userService.getUsers(userParamDto);
    }

    @DeleteMapping(path = "/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable @Positive Long userId) {
        userService.deleteUserById(userId);
    }

    @PatchMapping("/{userId}")
    public UserCommentAdminDto updateUserBan(
            @PathVariable @Positive Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Future LocalDateTime banDate) {
        return userService.updateUserBan(userId, banDate);
    }

    //----------proxy call-------------//

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable @Positive Long userId) {
        return UserMapper.toUserDto(userService.findById(userId));
    }
    
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Void> checkUserExists(@PathVariable @Positive Long userId) {
        return userService.checkUserExist(userId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{userId}/comments")
    public UserCommentAdminDto getUserCommentStats(@PathVariable @Positive Long userId) {
        return CommentMapper.toUserCommentAdminDto(userService.findById(userId));
    }

    @PatchMapping("/comments/{userId}")
    public UserCommentAdminDto setCommentsCount(@PathVariable @Positive Long userId,
                                                @RequestParam Integer commentsCount) {
        return CommentMapper.toUserCommentAdminDto(userService.updateUserCommentsCount(userId, commentsCount));
    }

    @PatchMapping("/{userId}/warning")
    public UserCommentAdminDto setWarningToUser(@PathVariable @Positive Long userId) {
        return CommentMapper.toUserCommentAdminDto(userService.setWarningToUser(userId));
    }

}
