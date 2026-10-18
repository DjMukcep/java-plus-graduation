package ru.practicum.exception;

public class EventServiceUnavailableException extends RuntimeException {
    public EventServiceUnavailableException(String eventId, Throwable cause) {
        super("Event service is unavailable for eventId=" + eventId, cause);
    }
}
