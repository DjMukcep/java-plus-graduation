package ru.practicum.proxy.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;

import java.util.List;

@Validated
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    @GetMapping("/admin/users/{userId}/comments")
    UserCommentAdminDto getUserCommentStats(@PathVariable @Positive Long userId);

    @PatchMapping("/admin/users/comments/{userId}")
    UserCommentAdminDto setCommentsCount(
            @PathVariable("userId") Long userId,
            @RequestParam("commentsCount") Integer commentsCount
    );

    @PatchMapping("/admin/users/{userId}/warning")
    UserCommentAdminDto setWarningToUser(@PathVariable @Positive Long userId);

    @GetMapping("/admin/users")
    List<UserDto> getUsers(@SpringQueryMap @Valid UserParamDto userParamDto);
}
