package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.config.RecommendationProperties;
import ru.practicum.dto.RecommendedEvent;
import ru.practicum.repository.action.UserAction;
import ru.practicum.repository.action.UserActionRepository;
import ru.practicum.repository.similarity.EventSimilarity;
import ru.practicum.repository.similarity.EventSimilarityRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {
    public final UserActionRepository userActionRepository;
    public final EventSimilarityRepository eventSimilarityRepository;
    private final RecommendationProperties recommendationProperties;

    public List<EventSimilarity> getSimilarEvents(Long eventId, Long userId, Integer maxResults) {
        List<EventSimilarity> similarities = eventSimilarityRepository.findSimilaritiesByEventId(eventId);
        Set<Long> interactedEventIds = userActionRepository.findInteractedEventIdsByUserId(userId);

        return similarities.stream()
                .filter(s -> {
                    Long otherEventId = s.getEventA().equals(eventId)
                            ? s.getEventB()
                            : s.getEventA();

                    return !otherEventId.equals(eventId) && !interactedEventIds.contains(otherEventId);
                })
                .sorted(Comparator.comparing(
                        EventSimilarity::getSimilarity,
                        Comparator.reverseOrder()
                ))
                .limit(maxResults)
                .toList();
    }

    public Map<Long,Double> getInteractionsCount(Set<Long> eventIds) {

        if (eventIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = userActionRepository
                .sumInteractionWeightsByEventIds(eventIds);

        Map<Long, Double> counts = rows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).doubleValue()
                ));

        eventIds.forEach(eventId -> counts.putIfAbsent(eventId, 0.0));

        log.info("Итоговые суммы взаимодействий: {}", counts);

        return counts;
    }

    public List<RecommendedEvent> getRecommendations(Long userId, Integer limit) {
        List<UserAction> userActions = userActionRepository.findByUserIdOrderByTimestampDesc(
                userId, PageRequest.of(0, limit));

        if (userActions.isEmpty()) {
            return List.of();
        }

        Set<Long> interactedEventsIds = userActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        List<EventSimilarity> similarities =
                eventSimilarityRepository.findSimilaritiesByEventIds(interactedEventsIds);

        Set<Long> allInteractedEventIds =
                userActionRepository.findInteractedEventIdsByUserId(userId);

        Map<Long, Double> recommendations = similarities.stream()
                .map(similarity -> {
                    Long eventA = similarity.getEventA();
                    Long eventB = similarity.getEventB();

                    Long recommendedEventId = interactedEventsIds.contains(eventA)
                            ? eventB
                            : eventA;

                    return Map.entry(recommendedEventId, similarity.getSimilarity());
                })
                .filter(entry -> !allInteractedEventIds.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Math::max
                ));

        return recommendations.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry ->
                        new RecommendedEvent(
                                entry.getKey(), calculatePredictedScore(userId, entry.getKey(),
                                recommendationProperties.neighborsCount())))
                .toList();
    }

    private Double calculatePredictedScore(Long userId, Long eventId, int k) {
        Set<Long> interactedEventIds =
                userActionRepository.findInteractedEventIdsByUserId(userId);

        List<EventSimilarity> neighbors = eventSimilarityRepository
                .findSimilaritiesByEventId(eventId)
                .stream()
                .filter(similarity -> {
                    Long otherEventId = similarity.getEventA().equals(eventId)
                            ? similarity.getEventB()
                            : similarity.getEventA();

                    return interactedEventIds.contains(otherEventId);
                })
                .sorted(Comparator.comparing(
                        EventSimilarity::getSimilarity,
                        Comparator.reverseOrder()
                ))
                .limit(k)
                .toList();

        if (neighbors.isEmpty()) {
            return 0.0;
        }

        Map<Long, Double> ratings = userActionRepository
                .findByUserIdAndEventIdIn(
                        userId,
                        neighbors.stream()
                                .map(similarity -> similarity.getEventA().equals(eventId)
                                        ? similarity.getEventB()
                                        : similarity.getEventA())
                                .collect(Collectors.toSet())
                )
                .stream()
                .collect(Collectors.toMap(
                        UserAction::getEventId,
                        UserAction::getRating
                ));

        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (EventSimilarity neighbor : neighbors) {
            Long neighborEventId = neighbor.getEventA().equals(eventId)
                    ? neighbor.getEventB()
                    : neighbor.getEventA();

            Double rating = ratings.get(neighborEventId);

            if (rating != null) {
                weightedSum += neighbor.getSimilarity() * rating;
                similaritySum += neighbor.getSimilarity();
            }
        }

        return similaritySum == 0.0 ? 0.0 : weightedSum / similaritySum;
    }
}
