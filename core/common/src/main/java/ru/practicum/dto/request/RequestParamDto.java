package ru.practicum.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestParamDto {
    @Positive
    private Long eventId;
    private Set<Long> ids;
}
