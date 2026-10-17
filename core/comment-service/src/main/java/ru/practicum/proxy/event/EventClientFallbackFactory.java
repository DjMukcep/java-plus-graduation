package ru.practicum.proxy.event;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    private final static Logger log = LoggerFactory.getLogger(EventClientFallbackFactory.class);

    @Override
    public EventClient create(Throwable cause) {

        return new EventClient() {

            @Override
            public boolean checkEventExistsAndPublished(Long eventId) {
                handleFailure(cause,eventId);
                return false;
            }

            @Override
            public boolean checkEventExists(Long eventId) {
                handleFailure(cause,eventId);
                return false;
            }

            private void handleFailure(Throwable cause, Long eventId) {
                if (cause instanceof FeignException feignException
                        && feignException.status() == 503) {
                    log.error("Ошибка при обращении к main-service для eventId={}: {}", eventId, cause.getMessage());
                }
            }
        };
    }
}
