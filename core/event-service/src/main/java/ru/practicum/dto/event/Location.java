package ru.practicum.dto.event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Location {

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;
}