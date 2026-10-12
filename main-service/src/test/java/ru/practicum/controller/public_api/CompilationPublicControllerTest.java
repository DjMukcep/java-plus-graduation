package ru.practicum.controller.public_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationsParamDto;
import ru.practicum.entity.compilation.CompilationService;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompilationPublicController.class)
class CompilationPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    private CompilationsParamDto paramDto;

    @BeforeEach
    public void setUp() {
        paramDto = CompilationsParamDto.builder()
                .from(0)
                .size(3)
                .pinned(null)
                .build();
    }

    @Test
    void getAll_shouldReturnListAndCorrectStatus() throws Exception {
        List<CompilationDto> expectedList = List.of(
                CompilationDto.builder().id(1L).title("Comp 1").build(),
                CompilationDto.builder().id(2L).title("Comp 2").build()
        );
        when(compilationService.getAll(any(CompilationsParamDto.class))).thenReturn(expectedList);

        mockMvc.perform(MockMvcRequestBuilders.get("/compilations")
                        .param("from", String.valueOf(paramDto.getFrom()))
                        .param("size", String.valueOf(paramDto.getSize())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[*].id", hasItems(1, 2)))
                .andExpect(jsonPath("$.[*].title", hasItems("Comp 1", "Comp 2")));

        verify(compilationService).getAll(any(CompilationsParamDto.class));
    }

    @Test
    void getById_shouldReturnDtoAndOkStatus() throws Exception {
        Long compId = 1L;
        CompilationDto expectedDto = CompilationDto.builder()
                .id(compId)
                .title("Single compilation")
                .build();
        when(compilationService.getById(eq(compId))).thenReturn(expectedDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/compilations/{compId}", compId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Single compilation"));

        verify(compilationService).getById(eq(compId));
    }

    @Test
    void getById_withInvalidId_shouldReturnBadRequest() throws Exception {
        Long invalidId = -1L;

        mockMvc.perform(MockMvcRequestBuilders.get("/compilations/{compId}", invalidId))
                .andExpect(status().isBadRequest());

         verifyNoInteractions(compilationService);
    }

    @Test
    void getAll_withInvalidSize_shouldReturnBadRequest() throws Exception {
        paramDto.setSize(-5);

        mockMvc.perform(MockMvcRequestBuilders.get("/compilations")
                        .param("size", String.valueOf(paramDto.getSize()))
                        .param("from", String.valueOf(paramDto.getFrom())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(compilationService);
    }

    @Test
    void getAll_withInvalidFrom_shouldReturnBadRequest() throws Exception {
        paramDto.setFrom(-5);

        mockMvc.perform(MockMvcRequestBuilders.get("/compilations")
                        .param("size", String.valueOf(paramDto.getSize()))
                        .param("from", String.valueOf(paramDto.getFrom())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(compilationService);
    }
}