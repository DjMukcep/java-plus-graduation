package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatClient {

    private final RestClient restClient;

    public void hit(EndpointHit endpointHit) {
        try {
            restClient.post()
                    .uri("/hit")
                    .body(endpointHit)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Stats service unavailable: {}", e.getMessage(), e);
        }
    }

    public List<ViewStats> getViewStats(StatsRequest statsRequest) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stats")
                            .queryParam("start", statsRequest.getStartTime())
                            .queryParam("end", statsRequest.getEndTime())
                            .queryParam("uris", statsRequest.getUris())
                            .queryParam("unique", statsRequest.getUnique())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStats>>() {
                    });
        } catch (RestClientException e) {
            log.error("Stats service unavailable: {}", e.getMessage(), e);
            return  Collections.emptyList();
        }
    }
}