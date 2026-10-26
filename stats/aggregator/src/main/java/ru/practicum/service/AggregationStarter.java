package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaTopicsProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import static java.lang.Math.*;


import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<Long, UserActionAvro> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaTopicsProperties topics;
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    // ключ мероприятие - значение другое мероприятие и их коэфф сходства
    private final Map<Long, Map<Long, Double>> eventSimilarities = new HashMap<>();

    //ключ юзер - значение мероприятие и его оценка
    private final Map<Long, Map<Long, Double>> userEventsWeights = new HashMap<>();


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

    private void consumeEvents() {
        while (true) {
            ConsumerRecords<Long, UserActionAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

            if (!records.isEmpty()) {
                processRecords(records);
                consumer.commitSync();
            }
        }
    }

    private void processRecords(ConsumerRecords<Long, UserActionAvro> records) {
        for (ConsumerRecord<Long, UserActionAvro> record : records) {
            processRecord(record);
        }
    }

    private void processRecord(ConsumerRecord<Long, UserActionAvro> record) {
        UserActionAvro userAction = record.value();
        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();
        ActionTypeAvro actionType = userAction.getActionType();

        log.info("new user action: {}", userAction);

        double incomeEventWeight = processEventWeight(actionType);
        double oldEventWeight = getOldWeight(userId, eventId);
        double newEventWeight = max(oldEventWeight, incomeEventWeight);
        boolean isEventWeightChanged = oldEventWeight != newEventWeight;

        if (isEventWeightChanged) {
            Map<Long, Double> eventsWeights = updateUserEventWeight(userId, eventId, newEventWeight);

            if (eventsWeights.size() > 1) {
                computeEventSimilarities(eventId, eventsWeights);
            }
        }
    }

    private Map<Long, Double> updateUserEventWeight(Long userId, Long eventId, double eventWeight) {
        Map<Long, Double> eventsWeights = userEventsWeights.computeIfAbsent(userId, id -> new HashMap<>());
        eventsWeights.put(eventId, eventWeight);
        log.info("user's event weights for userId: {} - {}",userId, eventsWeights);

        return eventsWeights;
    }


    private void computeEventSimilarities(Long eventId, Map<Long, Double> eventsWeights) {
        for (Long otherEventId : eventsWeights.keySet()) {
            if (eventId.equals(otherEventId)) {
                continue;
            }

            long eventIdA = min(eventId, otherEventId);
            long eventIdB = max(eventId, otherEventId);

            double sumA = getSumEvent(eventIdA);
            double sumB = getSumEvent(eventIdB);

            double score = getMinSum(eventIdA,eventIdB)/(sqrt(sumA) * sqrt(sumB));

            processEventsSimilarity(eventIdA, eventIdB, score);
        }
    }

    private void processEventsSimilarity(long eventA, long eventB, double score) {
        put(eventA, eventB, score);
        String eventsAB = eventA + "-" + eventB;

        EventSimilarityAvro event = EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(score)
                .setTimestamp(Instant.now())
                .build();

        sendEvent(eventsAB,event);
    }

    private double getOldWeight(Long userId, Long eventId) {
        return userEventsWeights.computeIfAbsent(
                userId, event -> new HashMap<>()).getOrDefault(eventId, 0.0);
    }

    private double processEventWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case ACTION_VIEW ->  0.4;
            case ACTION_REGISTER -> 0.8;
            case ACTION_LIKE -> 1.0;
        };
    }

    private double getSumEvent(Long eventId) {
        return userEventsWeights.values().stream()
                .map(userEvents -> userEvents.get(eventId))
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private double getMinSum(Long eventIdA, Long eventIdB) {
        return userEventsWeights.values().stream()
                .filter(events -> events.containsKey(eventIdA) && events.containsKey(eventIdB))
                .mapToDouble(events -> min(events.get(eventIdA), events.get(eventIdB)))
                .sum();
    }

    public void put(long eventA, long eventB, double score) {
        long first  = min(eventA, eventB);
        long second = max(eventA, eventB);

        eventSimilarities
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, score);
    }

    private void sendEvent(String eventsId, EventSimilarityAvro event) {
        log.info("send event: {}", event);
        producer.send(new ProducerRecord<>(
                topics.eventsSimilarity(),
                null,
                event.getTimestamp().toEpochMilli(),
                eventsId,
                event),
                this::onMessageSent
        );
    }

    private void onMessageSent(RecordMetadata metadata, Exception ex) {
        if (ex != null) {
            log.error(
                    "Ошибка отправки сообщения в топик {}",
                    metadata != null ? metadata.topic() : "unknown", ex
            );
        }
    }

    private void subscribe() {
        consumer.subscribe(Collections.singletonList(topics.userActions()));
        log.info("Успешная подписка на топик: {}", topics.userActions());
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    private void closeResources() {
        try {
            producer.flush();
            consumer.commitSync();
        } finally {
            log.info("Закрываем консьюмер");
            consumer.close();

            log.info("Закрываем продюсер");
            producer.close();
        }
    }
}
