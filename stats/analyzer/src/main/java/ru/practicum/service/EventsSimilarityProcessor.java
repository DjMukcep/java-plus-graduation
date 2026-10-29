package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaTopicsProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.repository.similarity.EventSimilarity;
import ru.practicum.repository.similarity.EventSimilarityRepository;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsSimilarityProcessor {

    private final Consumer<String, EventSimilarityAvro> consumer;
    private final KafkaTopicsProperties topics;
    private final EventSimilarityRepository repository;
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    public void start() {
        addShutdownHook();
        subscribe();

        try {
            consumeEvents();
        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения работы (wakeup).");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            closeResources();
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    private void subscribe() {
        consumer.subscribe(Collections.singletonList(topics.eventsSimilarity()));
        log.info("Успешная подписка на топик: {}", topics.eventsSimilarity());
    }

    private void consumeEvents() {
        while (true) {
            ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

            if (!records.isEmpty()) {
                processRecords(records);
                consumer.commitSync();
            }
        }
    }

    private void closeResources() {
        log.info("Закрываем консьюмер");
        consumer.close();
    }

    private void processRecords(ConsumerRecords<String, EventSimilarityAvro> records) {
        for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
            if (record == null) {
                continue;
            }
            processRecord(record.value());
        }
    }

    private void processRecord(EventSimilarityAvro record) {
        EventSimilarity similarity = EventSimilarity.builder()
                .eventA(record.getEventA())
                .eventB(record.getEventB())
                .similarity(record.getScore())
                .timestamp(record.getTimestamp())
                .build();

        repository.upsert(
                similarity.getEventA(),
                similarity.getEventB(),
                similarity.getSimilarity(),
                similarity.getTimestamp()
        );

        log.info("saved similarity: {}", similarity);
    }
}
