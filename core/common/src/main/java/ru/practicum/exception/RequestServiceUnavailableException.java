package ru.practicum.exception;

public class RequestServiceUnavailableException extends RuntimeException {
    public RequestServiceUnavailableException(String eventId, Throwable cause) {
        super("Request service is unavailable for eventId=" + eventId, cause);
    }
}
