package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.practicum.service.AggregationStarter;

@Component
@RequiredArgsConstructor
public class AggregationRunner implements ApplicationRunner {

    private final AggregationStarter starter;

    @Override
    public void run(ApplicationArguments args) {
        starter.start();
    }
}
