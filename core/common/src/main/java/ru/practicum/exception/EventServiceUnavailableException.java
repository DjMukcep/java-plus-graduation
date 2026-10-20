package ru.practicum.exception;

public class EventServiceUnavailableException extends RuntimeException {
    public EventServiceUnavailableException(String message) {
        super(message);
    }
}
