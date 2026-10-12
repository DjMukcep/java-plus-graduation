package ru.practicum.entity.event;

import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    @EntityGraph(attributePaths = {"category","initiator"})
    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"category","initiator"})
    Page<Event> findAll(@NonNull Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"category","initiator"})
    Page<Event> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndState(Long id, EventState state);

    boolean existsByCategoryId(Long categoryId);
}