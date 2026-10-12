package ru.practicum.entity.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String mail;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private CommentsRank rank;

    @Builder.Default
    @Column(name = "comments_count", nullable = false)
    private int commentsCount = 0;

    @Builder.Default
    @Column(name = "admin_warn", nullable = false)
    private Integer adminWarnings = 0;

    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean isWarningsLimitExceeded() {
        return adminWarnings >= 2;
    }

    public boolean isBanned() {
        return bannedUntil != null && getBannedUntil().isAfter(LocalDateTime.now());
    }
}
