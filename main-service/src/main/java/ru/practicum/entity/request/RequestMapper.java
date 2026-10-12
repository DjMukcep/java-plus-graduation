package ru.practicum.entity.request;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@UtilityClass
public class RequestMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Request toRequest(Event event, User user) {
        return Request.builder()
                .event(event)
                .user(user)
                .createdAt(LocalDateTime.now())
                .status(isStatusConfirmed(event) ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .build();
    }

    public static ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreatedAt().format(formatter))
                .eventId(request.getEvent().getId())
                .requesterId(request.getUser().getId())
                .status(request.getStatus().name())
                .build();
    }

    public static List<ParticipationRequestDto> toRequestDto(List<Request> requests) {
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    private boolean isStatusConfirmed(Event event) {
        return !event.getRequestModeration() || event.getParticipantLimit() == 0;
    }
}
