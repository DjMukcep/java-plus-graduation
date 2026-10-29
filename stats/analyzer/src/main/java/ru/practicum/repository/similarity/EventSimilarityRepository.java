package ru.practicum.repository.similarity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    @Query("""
        SELECT s
        FROM EventSimilarity s
        WHERE s.eventA = :eventId
           OR s.eventB = :eventId
        ORDER BY s.similarity DESC
        """)
    List<EventSimilarity> findSimilaritiesByEventId(@Param("eventId") Long eventId);


    @Query("""
        SELECT s
        FROM EventSimilarity s
        WHERE s.eventA IN :eventIds
           OR s.eventB IN :eventIds
        """)
    List<EventSimilarity> findSimilaritiesByEventIds(@Param("eventIds") Set<Long> eventIds);


    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO similarities (event_a, event_b, similarity, created_at)
        VALUES (:eventA, :eventB, :similarity, :createdAt)
        ON CONFLICT (event_a, event_b)
        DO UPDATE SET
            similarity = EXCLUDED.similarity,
            created_at = EXCLUDED.created_at
        """, nativeQuery = true)
    void upsert(
            @Param("eventA") Long eventA,
            @Param("eventB") Long eventB,
            @Param("similarity") Double similarity,
            @Param("createdAt") Instant createdAt
    );
}
