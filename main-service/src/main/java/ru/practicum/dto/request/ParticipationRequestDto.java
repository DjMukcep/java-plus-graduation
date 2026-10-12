package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipationRequestDto {
    private Long id;
    private String created;

    @JsonProperty("event")
    private Long eventId;

    @JsonProperty("requester")
    private Long requesterId;
    private String status;
}
