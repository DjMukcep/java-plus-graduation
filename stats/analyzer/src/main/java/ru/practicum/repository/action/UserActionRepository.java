package ru.practicum.repository.action;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    @Query("""
        SELECT ua
        FROM UserAction ua
        WHERE ua.userId = :userId
          AND ua.eventId IN :eventIds
        """)
    List<UserAction> findByUserIdAndEventIdIn(@Param("userId") Long userId,
                                              @Param("eventIds") Set<Long> eventIds);

    @Query("""
        SELECT DISTINCT ua.eventId
        FROM UserAction ua
        WHERE ua.userId = :userId
        """)
    Set<Long> findInteractedEventIdsByUserId(@Param("userId") Long userId);

    @Query(value = """
        SELECT event_id, COALESCE(SUM(rating)::DOUBLE PRECISION, 0.0) AS total
        FROM interactions
        WHERE event_id IN (:eventIds)
        GROUP BY event_id
        """, nativeQuery = true)
    List<Object[]> sumInteractionWeightsByEventIds(@Param("eventIds") Set<Long> eventIds);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO interactions (user_id, event_id, rating, created_at)
        VALUES (:userId, :eventId, :rating, :createdAt)
            ON CONFLICT (user_id, event_id)
            DO UPDATE SET
            rating = GREATEST(interactions.rating, EXCLUDED.rating),
            created_at = EXCLUDED.created_at
        """, nativeQuery = true)
    void upsert(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId,
            @Param("rating") Double rating,
            @Param("createdAt") Instant createdAt
    );
}
