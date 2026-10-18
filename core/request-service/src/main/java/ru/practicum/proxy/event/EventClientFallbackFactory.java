package ru.practicum.proxy.event;


import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.exception.RequestServiceUnavailableException;

@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    private final static Logger log = LoggerFactory.getLogger(EventClientFallbackFactory.class);

    @Override
    public EventClient create(Throwable cause) {
        return (eventId) -> {
            if (cause instanceof FeignException feignException
                    && (feignException.status() == 404
                    || feignException.status() == 409)) {

                throw feignException;
            }

            log.error("Event service is unavailable for eventId={}", eventId, cause);

            throw new RequestServiceUnavailableException(eventId.toString(), cause);
        };
    }
}
