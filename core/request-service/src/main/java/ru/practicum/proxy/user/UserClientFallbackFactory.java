package ru.practicum.proxy.user;


import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.UserServiceUnavailableException;


@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    private final static Logger log = LoggerFactory.getLogger(UserClientFallbackFactory.class);

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

            private UserServiceUnavailableException handleFailure(Throwable cause, Long userId) {
                if (cause instanceof FeignException feignException
                        && feignException.status() == 404) {
                    throw feignException;
                }

                log.error("Ошибка при обращении к user-service для userId={}: {}", userId, cause.getMessage());

                return new UserServiceUnavailableException("User service is unavailable for userId=" + userId);
            }

        };
    }
}
