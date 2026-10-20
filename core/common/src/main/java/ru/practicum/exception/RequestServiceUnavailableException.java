package ru.practicum.exception;

public class RequestServiceUnavailableException extends RuntimeException {
    public RequestServiceUnavailableException(String message) {
        super(message);
    }
}
