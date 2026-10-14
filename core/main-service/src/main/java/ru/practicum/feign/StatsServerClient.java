package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.dto.stats.StatsRequest;
import ru.practicum.dto.stats.ViewStats;

import java.util.List;

@FeignClient(name = "stats-server", fallbackFactory = StatsServerClientFallbackFactory.class)
public interface StatsServerClient {

    @PostMapping("/hit")
    void hit(@RequestBody EndpointHit endpointHit);

    @GetMapping("/stats")
    List<ViewStats> getViewStats(@SpringQueryMap StatsRequest request);
}
