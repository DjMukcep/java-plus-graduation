package ru.practicum.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.dto.stats.StatsRequest;
import ru.practicum.dto.stats.ViewStats;


import java.util.Collections;
import java.util.List;

@Component
public class StatsServerClientFallbackFactory implements FallbackFactory<StatsServerClient> {

    private final static Logger log = LoggerFactory.getLogger(StatsServerClientFallbackFactory.class);

    @Override
    public StatsServerClient create(Throwable cause) {
        return new StatsServerClient() {

            @Override
            public void hit(EndpointHit endpointHit) {
                log.warn("Failed to send endpoint hit to stats-server");
            }

            @Override
            public List<ViewStats> getViewStats(StatsRequest request) {
                log.warn("Stats service unavailable: {}", cause.getMessage(), cause);
                return Collections.emptyList();
            }
        };
    }
}
