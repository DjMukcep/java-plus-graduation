package ru.practicum.controller.private_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.request.RequestService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestPrivateController.class)
class RequestPrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(1L);

        when(requestService.saveRequest(1L, 2L))
                .thenReturn(dto);

        mockMvc.perform(post("/users/1/requests")
                        .param("eventId", "2"))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(requestService).saveRequest(1L, 2L);
    }

    @Test
    void cancelRequest_shouldReturnCanceledRequest() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(1L);

        when(requestService.cancelRequest(1L, 10L))
                .thenReturn(dto);

        mockMvc.perform(patch("/users/1/requests/10/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(requestService).cancelRequest(1L, 10L);
    }

    @Test
    void getUserRequests_shouldReturnRequests() throws Exception {
        List<ParticipationRequestDto> requests = List.of(
                new ParticipationRequestDto(),
                new ParticipationRequestDto()
        );

        when(requestService.getUserRequests(1L))
                .thenReturn(requests);

        mockMvc.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(requests)));

        verify(requestService).getUserRequests(1L);
    }

    @Test
    void createRequest_shouldReturnBadRequest_whenUserIdIsInvalid() throws Exception {
        mockMvc.perform(post("/users/0/requests")
                        .param("eventId", "2"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestService);
    }

    @Test
    void createRequest_shouldReturnBadRequest_whenEventIdIsInvalid() throws Exception {
        mockMvc.perform(post("/users/1/requests")
                        .param("eventId", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestService);
    }

    @Test
    void cancelRequest_shouldReturnBadRequest_whenRequestIdIsInvalid() throws Exception {
        mockMvc.perform(patch("/users/1/requests/0/cancel"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestService);
    }

}