package ru.practicum.internal.user;


import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParamDto;
import ru.practicum.exception.UserServiceUnavailableException;

import java.util.List;

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallbackFactory.class);

    @Override
    public UserClient create(Throwable cause) {
        return new  UserClient() {

            @Override
            public UserDto getUserById(Long userId) {
                throw handleFailure(cause,userId);
            }

            @Override
            public void checkUserExists(Long userId) {
                throw handleFailure(cause,userId);
            }

            @Override
            public List<UserDto> getUsers(UserParamDto userParamDto) {
                log.error("Ошибка при получении списка пользователей ids: {}. Cause: {}",
                        userParamDto.getIds(), cause.getMessage());
                return List.of();
            }

            private UserServiceUnavailableException handleFailure(Throwable cause, Long userId) {
                if (cause instanceof FeignException feignException
                        && feignException.status() == 404) {
                    throw feignException;
                }

                log.error("Ошибка при обращении к user-service для userId={}. Cause: {}", userId, cause.getMessage());

                return new UserServiceUnavailableException("User service is unavailable for userId=" + userId);
            }

        };
    }
}
