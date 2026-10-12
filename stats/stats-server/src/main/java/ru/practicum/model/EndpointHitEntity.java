package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hits")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String app;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(name = "hit_timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointHitEntity)) return false;
        return id != null && id.equals(((EndpointHitEntity) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}