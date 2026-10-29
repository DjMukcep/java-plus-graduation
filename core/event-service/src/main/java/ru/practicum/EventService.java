package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.practicum.internal.config.UserClientConfig;


@Import(UserClientConfig.class)
@SpringBootApplication
public class EventService {
    public static void main(String[] args) {
        SpringApplication.run(EventService.class, args);
    }
}