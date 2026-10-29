package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Component
public class KafkaActionSender {

    private final Producer<Long, SpecificRecordBase> producer;

    public void sendMessage(
            String topic, Long userId, Instant timestamp, SpecificRecordBase message) {
        producer.send(new ProducerRecord<>(
                topic, null, timestamp.toEpochMilli(), userId, message),
                this::onMessageSent);
    }

    private void onMessageSent(RecordMetadata metadata, Exception ex) {
        if (ex != null) {
            log.error(
                    "Ошибка отправки сообщения в топик {}",
                    metadata != null ? metadata.topic() : "unknown", ex
            );
        } else  {
            log.info("Сообщение отправлено. topic={}, partition={}, offset={}",
                    metadata.topic(), metadata.partition(), metadata.offset());
        }
    }
}
