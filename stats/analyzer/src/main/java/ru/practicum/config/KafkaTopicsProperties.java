package ru.practicum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics")
public record KafkaTopicsProperties(String eventsSimilarity, String userActions) {
}
