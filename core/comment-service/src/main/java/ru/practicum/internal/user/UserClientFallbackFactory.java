package ru.practicum.internal.user;


import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.comment.UserCommentAdminDto;
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
            public UserCommentAdminDto getUserCommentStats(Long userId) {
                throw handleFailure(cause,userId);
            }

            @Override
            public UserCommentAdminDto setCommentsCount(Long userId, Integer commentsCount) {
                throw handleFailure(cause,userId);
            }

            @Override
            public UserCommentAdminDto setWarningToUser(Long userId) {
                throw handleFailure(cause,userId);
            }

            @Override
            public List<UserDto> getUsers(UserParamDto userParamDto) {
                log.error("Ошибка при получении списка пользователей ids: {}", userParamDto.getIds());
                return List.of();
            }

            private UserServiceUnavailableException handleFailure(Throwable cause, Long userId) {
                if (cause instanceof FeignException feignException
                        && feignException.status() == 503) {
                    throw feignException;
                }

                log.error("Ошибка при обращении к user-service для userId={}. Cause: {}", userId, cause.getMessage());

                return new UserServiceUnavailableException("User service is unavailable for userId=" + userId);
            }

        };
    }
}
