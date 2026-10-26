package ru.practicum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.consumer")
public record KafkaConsumerProperties(String groupId, Boolean autoCommit) {
}
