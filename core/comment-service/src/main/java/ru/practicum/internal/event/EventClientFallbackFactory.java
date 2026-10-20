package ru.practicum.internal.event;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.exception.EventServiceUnavailableException;

@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    private static final Logger log = LoggerFactory.getLogger(EventClientFallbackFactory.class);

    @Override
    public EventClient create(Throwable cause) {

        return new EventClient() {

            @Override
            public boolean checkEventExistsAndPublished(Long eventId) {
                throw handleFailure(cause,eventId);
            }

            @Override
            public boolean checkEventExists(Long eventId) {
                throw handleFailure(cause,eventId);

            }

            private EventServiceUnavailableException handleFailure(Throwable cause, Long eventId) {
                if (cause instanceof FeignException feignException
                        && feignException.status() == 503) {
                    log.error("Ошибка при обращении к event-service для eventId={}. Cause: {}",
                            eventId, cause.getMessage());
                }

                return new EventServiceUnavailableException("Event service is unavailable for event-id=" + eventId);
            }
        };
    }
}
