package ru.practicum.entity.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    @EntityGraph(attributePaths = {"author","event"})
    Optional<Comment> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"author","event"})
    List<Comment> findAllByAuthorId(Long id);

    @EntityGraph(attributePaths = {"author","event"})
    List<Comment> findAllByEventId(Long eventId);

    @EntityGraph(attributePaths = {"author","event"})
    List<Comment> findAllByTextContainsIgnoreCase(String text, Pageable pageable);
}
