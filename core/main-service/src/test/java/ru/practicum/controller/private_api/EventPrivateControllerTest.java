package ru.practicum.controller.private_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.event.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventPrivateController.class)
class EventPrivateControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EventService eventService;

    private EventFullDto fullDto;
    private EventShortDto shortDto;
    private ParticipationRequestDto requestDto;
    private EventRequestStatusUpdateResult updateResult;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @BeforeEach
    void setUp() {
        fullDto = EventFullDto.builder()
                .id(1L)
                .title("Title")
                .state("PENDING")
                .build();

        shortDto = EventShortDto.builder()
                .id(1L)
                .title("Title")
                .build();

        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .build();

        updateResult = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(List.of())
                .build();
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        NewEventDto dto = validNewEventDto();

        when(eventService.create(eq(1L), any(NewEventDto.class)))
                .thenReturn(fullDto);

        mvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"));

        verify(eventService).create(eq(1L), any(NewEventDto.class));
    }

    @Test
    void getById_shouldReturnEvent() throws Exception {
        when(eventService.getById(1L, 2L)).thenReturn(fullDto);

        mvc.perform(get("/users/1/events/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(eventService).getById(1L, 2L);
    }

    @Test
    void getUserEvents_shouldReturnEvents() throws Exception {
        when(eventService.getUserEvents(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(shortDto));

        mvc.perform(get("/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(eventService).getUserEvents(eq(1L), any(Pageable.class));
    }

    @Test
    void updateEventByCreatorId_shouldReturnUpdatedEvent() throws Exception {
        UpdateEventUserRequest request = UpdateEventUserRequest.builder().build();

        when(eventService.updateEventByCreatorId(any(EventParamDto.class),
                any(UpdateEventUserRequest.class)))
                .thenReturn(fullDto);

        mvc.perform(patch("/users/1/events/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(eventService)
                .updateEventByCreatorId(any(EventParamDto.class),
                        any(UpdateEventUserRequest.class));
    }

    @Test
    void getEventRequestsByCreatorId_shouldReturnRequests() throws Exception {
        when(eventService.getEventRequestsByCreatorId(1L, 2L))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/users/1/events/2/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(eventService).getEventRequestsByCreatorId(1L, 2L);
    }

    @Test
    void updateEventRequestsStatus_shouldReturnResult() throws Exception {
        EventRequestStatusUpdateRequest request =
                EventRequestStatusUpdateRequest.builder().build();

        when(eventService.updateEventRequestsStatus(any(EventParamDto.class),
                any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(updateResult);

        mvc.perform(patch("/users/1/events/2/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(eventService)
                .updateEventRequestsStatus(any(EventParamDto.class),
                        any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void create_whenUserIdIsNegative_shouldReturn400() throws Exception {
        mvc.perform(post("/users/-1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserEvents_whenSizeIsZero_shouldReturn400() throws Exception {
        mvc.perform(get("/users/1/events")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserEvents_whenFromIsNegative_shouldReturn400() throws Exception {
        mvc.perform(get("/users/1/events")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    private NewEventDto validNewEventDto() {
        return NewEventDto.builder()
                .annotation("Annotation more than twenty symbols")
                .description("Description more than twenty symbols for validation.")
                .category(1L)
                .eventDate(LocalDateTime.now().plusDays(2).format(formatter))
                .location(new Location(55.75f, 37.61f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Title")
                .build();
    }
}