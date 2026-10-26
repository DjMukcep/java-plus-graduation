package ru.practicum.repository.action;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "interactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "created_at", nullable = false)
    private Instant timestamp;

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UserAction)) return false;
        return id != null && id.equals(((UserAction) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
