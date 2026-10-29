package ru.practicum.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.deserializer.UserActionDeserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import serializer.AvroSerializer;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class AggregatorKafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean("aggregator-consumer")
    public Consumer<Long, UserActionAvro> avroConsumer(KafkaConsumerProperties properties) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.groupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.autoCommit());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    @Bean("aggregator-producer")
    public Producer<String, SpecificRecordBase> avroProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);
        return  new KafkaProducer<>(props);
    }
}
