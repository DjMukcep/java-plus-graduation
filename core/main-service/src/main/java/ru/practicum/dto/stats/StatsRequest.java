package ru.practicum.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StatsRequest {
    private String start;
    private String end;
    private List<String> uris;
    private Boolean unique;
}
