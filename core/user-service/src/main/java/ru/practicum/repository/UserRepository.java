package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.entity.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByMail(String email);

    List<User> findAllByIdIn(List<Long> ids);
}
