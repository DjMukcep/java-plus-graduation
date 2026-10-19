package ru.practicum.proxy.event;


import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.exception.EventServiceUnavailableException;
import ru.practicum.exception.RequestServiceUnavailableException;

@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    private final static Logger log = LoggerFactory.getLogger(EventClientFallbackFactory.class);

    @Override
    public EventClient create(Throwable cause) {
        return (eventId) -> {
            if (cause instanceof FeignException feignException && (feignException.status() == 404)) {
                throw feignException;
            }

            log.error("Event service is unavailable for eventId={}. Cause: {}", eventId, cause.getMessage());

            throw new EventServiceUnavailableException("Event service is unavailable for eventId=" + eventId);
        };
    }
}
