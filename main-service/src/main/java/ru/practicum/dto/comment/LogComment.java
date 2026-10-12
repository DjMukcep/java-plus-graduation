package ru.practicum.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogComment {
    private Long id;
    private String text;

    private Long authorId;
    private Long eventId;

    private String authorName;

    private String created;
    private String updated;
}
