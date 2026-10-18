package ru.practicum.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StatusRequestsDto {
    private Set<Long> eventIds;
    private RequestStatus requestStatus;
}
