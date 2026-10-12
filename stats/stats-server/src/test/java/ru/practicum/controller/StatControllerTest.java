package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.EndpointHit;
import ru.practicum.ParamDto;
import ru.practicum.service.StatService;

import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(StatController.class)
class StatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatService statService;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    void create_shouldReturn201_andCallService() throws Exception {
        EndpointHit request = EndpointHit.builder()
                .app("app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("2026-01-01 10:00:00")
                .build();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(statService).saveHit(any(EndpointHit.class));
    }


    @Test
    void create_shouldReturn400_whenInvalidBody() throws Exception {
        EndpointHit request = EndpointHit.builder()
                .app("") // invalid
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp("2026-01-01 10:00:00")
                .build();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(statService, never()).saveHit(any());
    }

    @Test
    void getStats_shouldReturn200_andCallService() throws Exception {

        when(statService.getStats(any())).thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", "2026-01-01 10:00:00")
                        .param("end", "2026-01-02 10:00:00")
                        .param("uris", "/test")
                        .param("unique", "true"))
                .andExpect(status().isOk());

        verify(statService).getStats(any(ParamDto.class));
    }

    @Test
    void getStats_shouldReturn400_whenStartIsNull() throws Exception {

        mockMvc.perform(get("/stats")
                        .param("endTime", "2026-01-02 10:00:00"))
                .andExpect(status().isBadRequest());

        verify(statService, never()).getStats(any());
    }
}