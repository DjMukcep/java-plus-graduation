package practicum;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.util.List;

@FeignClient(name = "stats-server", fallbackFactory = StatsClientFallbackFactory.class)
public interface StatsClient {

    @PostMapping("/hit")
    void hit(@RequestBody EndpointHit endpointHit);

    @GetMapping("/stats")
    List<ViewStats> getViewStats(@SpringQueryMap StatsRequest request);
}
