package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ru.practicum.dto.event.LogEventShort;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogCompilation {
    private Long id;
    private Boolean pinned;
    private String title;
    private Set<LogEventShort> events;
}
