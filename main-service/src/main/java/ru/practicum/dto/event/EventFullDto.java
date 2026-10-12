package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.category.CategoryDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFullDto {

    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private String eventDate;
    private String publishedOn;
    private String createdOn;
    private Integer participantLimit;
    private Boolean paid;
    private UserShortDto initiator;
    private Location location;
    private Boolean requestModeration;
    private String state;
    private Long views;
    private Long confirmedRequests;
}