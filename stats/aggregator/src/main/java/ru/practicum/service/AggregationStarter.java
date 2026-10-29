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

    // Сумма весов мероприятия
    private final Map<Long, Double> eventWeights = new HashMap<>();

    // Суммы минимумов для пар мероприятий
    private final Map<Long, Map<Long, Double>> minSums = new HashMap<>();


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
        logUserAction(userAction);

        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();

        Map<Long, Double> userWeights = getUserWeights(userId);

        double oldWeight = userWeights.getOrDefault(eventId, 0.0);
        double newWeight = max(oldWeight, processEventWeight(userAction.getActionType()));

        if (oldWeight == newWeight) {
            return;
        }

        updateEventWeights(eventId, userWeights, oldWeight, newWeight);

        computeEventSimilarities(eventId, userWeights, userAction.getTimestamp());
    }

    private void logUserAction(UserActionAvro userAction) {
        log.info("Обработка действия: userId={}, eventId={}, actionType={}",
                userAction.getUserId(),
                userAction.getEventId(),
                userAction.getActionType());
    }

    private Map<Long, Double> getUserWeights(Long userId) {
        return userEventsWeights.computeIfAbsent(userId, id -> new HashMap<>());
    }

    private void updateEventWeights(
            Long eventId,
            Map<Long, Double> userWeights,
            double oldWeight,
            double newWeight
    ) {
        userWeights.put(eventId, newWeight);

        eventWeights.merge(eventId, newWeight - oldWeight, Double::sum);

        updateMinSums(eventId, userWeights, oldWeight, newWeight);
    }

    private void updateMinSums(
            Long eventId,
            Map<Long, Double> userWeights,
            double oldWeight,
            double newWeight
    ) {
        for (Long otherEventId : userWeights.keySet()) {
            if (eventId.equals(otherEventId)) {
                continue;
            }

            double otherWeight = userWeights.get(otherEventId);

            double oldMin = min(oldWeight, otherWeight);
            double newMin = min(newWeight, otherWeight);
            double delta = newMin - oldMin;

            if (delta != 0.0) {
                updateMinSum(eventId, otherEventId, delta);
            }
        }
    }

    private void updateMinSum(long eventA, long eventB, double delta) {
        long first = min(eventA, eventB);
        long second = max(eventA, eventB);

        minSums.computeIfAbsent(first, id -> new HashMap<>())
                .merge(second, delta, Double::sum);

        log.info(
                "Update minSum: eventA={}, eventB={}, delta={}, minSum={}",
                first, second, delta,
                minSums.get(first).get(second)
        );
    }

//    private void computeEventSimilarities(
//            Long eventId,
//            Map<Long, Double> userWeights,
//            Instant timestamp
//    ) {
//        double sumA = eventWeights.get(eventId);
//
//        for (Long otherEventId : userWeights.keySet()) {
//            if (eventId.equals(otherEventId)) {
//                continue;
//            }
//
//            double sumB = eventWeights.get(otherEventId);
//
//            double minSum = minSums
//                    .getOrDefault(min(eventId, otherEventId), Map.of())
//                    .getOrDefault(max(eventId, otherEventId), 0.0);
//
//            if (minSum == 0) {
//                continue;
//            }
//
//            double score = minSum / (sqrt(sumA) * sqrt(sumB));
//
//            long eventIdA = min(eventId, otherEventId);
//            long eventIdB = max(eventId, otherEventId);
//
//            processEventsSimilarity(eventIdA, eventIdB, score, timestamp);
//        }
//    }

    private void computeEventSimilarities(
            Long eventId,
            Map<Long, Double> userWeights,
            Instant timestamp
    ) {
        double sumA = eventWeights.get(eventId);

        for (Long otherEventId : eventWeights.keySet()) {
            if (eventId.equals(otherEventId)) {
                continue;
            }

            double sumB = eventWeights.get(otherEventId);

            double minSum = minSums
                    .getOrDefault(min(eventId, otherEventId), Map.of())
                    .getOrDefault(max(eventId, otherEventId), 0.0);

            if (minSum == 0) {
                continue;
            }

            double score = minSum / (sqrt(sumA) * sqrt(sumB));

            long eventIdA = min(eventId, otherEventId);
            long eventIdB = max(eventId, otherEventId);

            put(eventIdA, eventIdB, score);

            if (userWeights.containsKey(otherEventId)) {
                processEventsSimilarity(eventIdA, eventIdB, score, timestamp);
            }
        }
    }

    private void processEventsSimilarity(long eventA, long eventB, double score, Instant timestamp) {
        put(eventA, eventB, score);
        String eventsAB = eventA + "-" + eventB;

        EventSimilarityAvro event = EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();

        sendEvent(eventsAB,event);
    }

    private double processEventWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW ->  0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
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
