package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorId(Long id);

    List<Comment> findAllByEventId(Long eventId);

    List<Comment> findAllByTextContainsIgnoreCase(String text, Pageable pageable);
}
