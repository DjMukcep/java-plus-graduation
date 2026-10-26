package ru.practicum.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import serializer.AvroSerializer;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class CollectorKafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean("collector-producer")
    public Producer<Long, SpecificRecordBase> avroProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);
        return  new KafkaProducer<>(props);
    }
}
