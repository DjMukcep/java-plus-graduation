package ru.practicum.proxy.request;

import feign.FeignException;
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
                if (cause instanceof FeignException feignException
                        && (feignException.status() == 404
                        || feignException.status() == 409)) {

                    throw feignException;
                }

                log.error("Request service is unavailable for eventId={}", requestDto.getEventId(), cause);

                throw new RequestServiceUnavailableException(requestDto.getEventId().toString(), cause);
            }

            @Override
            public List<ParticipationRequestDto> getUserRequestsByEventId(Long eventId) {
                if (cause instanceof FeignException feignException
                        && (feignException.status() == 404
                        || feignException.status() == 409)) {

                    throw feignException;
                }

                log.error("Request service is unavailable for eventId={}", eventId, cause);

                throw new RequestServiceUnavailableException(eventId.toString(), cause);
            }

            @Override
            public ParticipationRequestDto updateStatus(RequestStatusUpdate requestStatusUpdate) {
                if (cause instanceof FeignException feignException
                        && (feignException.status() == 404
                        || feignException.status() == 409)) {

                    throw feignException;
                }

                log.error("Request service is unavailable for requestId={}", requestStatusUpdate.getRequestId(), cause);

                throw new RequestServiceUnavailableException(requestStatusUpdate.getRequestId().toString(), cause);
            }

            @Override
            public long countByEventIdAndStatus(Long eventId, RequestStatus status) {
                if (cause instanceof FeignException feignException
                        && (feignException.status() == 404
                        || feignException.status() == 409)) {

                    throw feignException;
                }

                log.error("Request service is unavailable for eventId={}", eventId, cause);

                throw new RequestServiceUnavailableException(eventId.toString(), cause);
            }

            @Override
            public List<EventRequestsCountDto> countByEventIdsAndStatus(StatusRequestsDto statusRequestsDto) {
                if (cause instanceof FeignException feignException
                        && (feignException.status() == 404
                        || feignException.status() == 409)) {

                    throw feignException;
                }

                log.error("Request service is unavailable for eventIds={}", statusRequestsDto.getEventIds(), cause);

                throw new RequestServiceUnavailableException(statusRequestsDto.getEventIds().toString(), cause);
            }
        };
    }
}
