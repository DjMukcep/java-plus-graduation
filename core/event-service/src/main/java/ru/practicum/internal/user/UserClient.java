package ru.practicum.internal.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;

import java.util.List;

@Validated
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    @GetMapping("/admin/users/{userId}")
    UserDto getUserById(@PathVariable @Positive Long userId);

    @GetMapping("/admin/users/{userId}/exists")
    void checkUserExists(@PathVariable @Positive Long userId);

    @GetMapping("/admin/users")
    List<UserDto> getUsers(@SpringQueryMap @Valid UserParamDto userParamDto);
}
