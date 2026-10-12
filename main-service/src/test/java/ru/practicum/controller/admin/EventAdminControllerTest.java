package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.entity.event.AdminStateAction;
import ru.practicum.entity.event.EventService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventAdminController.class)
class EventAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getEventsByAdmin_shouldReturnList() throws Exception {
        when(eventService.getEventsByAdmin(any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void updateEventByAdmin_shouldReturnUpdatedEvent() throws Exception {
        EventFullDto dto = EventFullDto.builder()
                .id(1L)
                .state("PUBLISHED")
                .build();

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        when(eventService.updateEventByAdmin(any(), any())).thenReturn(dto);

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
    }

    @Test
    void updateEventByAdmin_withEmptyBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}