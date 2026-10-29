package ru.practicum.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.deserializer.EventSimilarityDeserializer;
import ru.practicum.deserializer.UserActionDeserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class AnalyzerKafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Consumer<String, EventSimilarityAvro> EventSimilarityConsumer(KafkaConsumerProperties properties) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.groupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.autoCommit());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    @Bean
    public Consumer<String, UserActionAvro> UserActionConsumer(KafkaConsumerProperties properties) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.groupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.autoCommit());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        return new KafkaConsumer<>(props);
    }
}
