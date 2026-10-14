package practicum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;


import java.util.Collections;
import java.util.List;

@Component
public class StatsClientFallbackFactory implements FallbackFactory<StatsClient> {

    private final static Logger log = LoggerFactory.getLogger(StatsClientFallbackFactory.class);

    @Override
    public StatsClient create(Throwable cause) {
        return new StatsClient() {

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
