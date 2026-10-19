package ru.practicum.proxy.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.event.EventRequestsCountDto;
import ru.practicum.dto.request.*;
import ru.practicum.exception.RequestServiceUnavailableException;

import java.util.List;

@Component
public class RequestClientFallbackFactory implements FallbackFactory<RequestClient> {

    private final static Logger log = LoggerFactory.getLogger(RequestClientFallbackFactory.class);

    @Override
    public RequestClient create(Throwable cause) {
        return new RequestClient() {

            @Override
            public List<ParticipationRequestDto> getUserRequestsByUserIdsAndEventId(RequestParamDto requestDto) {
                logError(requestDto.getEventId(),  cause);
                return List.of();
            }

            @Override
            public List<ParticipationRequestDto> getUserRequestsByEventId(Long eventId) {
                logError(eventId,cause);
                return List.of();
            }

            @Override
            public List<ParticipationRequestDto> updateStatusRequests(StatusRequestsDto statusRequestsDto) {
                log.error("Request service is unavailable for requestIds={}. Cause: {}",
                        statusRequestsDto.getRequestIds(), cause.getMessage());

                throw new RequestServiceUnavailableException(
                        "Request service is unavailable for eventIds=" + statusRequestsDto.getRequestIds());
            }

            @Override
            public long countByEventIdAndStatus(Long eventId, RequestStatus status) {
                logError(eventId,cause);
                return 0;
            }

            @Override
            public List<EventRequestsCountDto> countByEventIdsAndStatus(StatusRequestsDto statusRequestsDto) {
                log.error("Request service is unavailable for eventIds={}. Cause: {}",
                        statusRequestsDto.getRequestIds(), cause.getMessage());

                return List.of();
            }

            private void logError(Long eventId, Throwable cause) {
                log.error("Request service is unavailable for eventId={}. Cause: {}",
                        eventId, cause.getMessage());
            }
        };
    }
}
