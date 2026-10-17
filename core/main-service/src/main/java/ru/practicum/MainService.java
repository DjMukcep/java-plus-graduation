package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import practicum.StatsClientConfig;
import org.springframework.context.annotation.Import;
import ru.practicum.proxy.config.UserClientConfig;


@Import({StatsClientConfig.class, UserClientConfig.class})
@SpringBootApplication
public class MainService {
    public static void main(String[] args) {
        SpringApplication.run(MainService.class, args);
    }
}