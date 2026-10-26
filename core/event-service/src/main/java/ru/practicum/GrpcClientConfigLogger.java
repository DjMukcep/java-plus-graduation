package ru.practicum;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcClientConfigLogger {

    private final Environment environment;

    @PostConstruct
    void logGrpcConfiguration() {
        log.info("gRPC analyzer address: {}",
                environment.getProperty("grpc.client.analyzer.address"));
        log.info("gRPC analyzer negotiationType: {}",
                environment.getProperty("grpc.client.analyzer.negotiationType"));
    }
}
