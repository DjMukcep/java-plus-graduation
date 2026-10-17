package ru.practicum.exception;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(Long userId,Throwable cause) {
        super("User service is unavailable for userId=" + userId, cause);
    }
}
