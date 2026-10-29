package ru.practicum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer.recommendations")
public record RecommendationProperties(int neighborsCount) {
}
