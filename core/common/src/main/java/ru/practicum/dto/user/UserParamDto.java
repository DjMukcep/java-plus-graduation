package ru.practicum.dto.user;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.Set;

@Data
public class UserParamDto {
    private Set<Long> ids;

    @PositiveOrZero
    private int from = 0;

    @PositiveOrZero
    private int size = 10;
}
