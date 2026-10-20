package ru.practicum.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusUpdate {
    @Positive
    private Long requestId;
    private RequestStatus requestStatus;
}
