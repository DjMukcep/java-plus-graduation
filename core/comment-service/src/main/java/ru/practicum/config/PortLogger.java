package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortLogger {

    private final Environment environment;

    @EventListener
    public void onWebServerInitialized(
            WebServerInitializedEvent event) {
        log.info("Actual server port: {}", event.getWebServer().getPort());
        log.info("Configured server.port: {}",
                environment.getProperty("server.port"));
    }
}
