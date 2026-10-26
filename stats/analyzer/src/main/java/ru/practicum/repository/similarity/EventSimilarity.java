package ru.practicum.repository.similarity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "similarities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(nullable = false)
    private Double similarity;

    @Column(name = "created_at", nullable = false)
    private Instant timestamp;

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof EventSimilarity)) return false;
        return id != null && id.equals(((EventSimilarity) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
