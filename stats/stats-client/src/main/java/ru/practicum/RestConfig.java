package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestConfig {

    @Bean
    RestClient restClient(RestClient.Builder builder, @Value("${client.url}") String clientUrl) {
        return builder.baseUrl(clientUrl).build();
    }
}