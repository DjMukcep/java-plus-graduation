package ru.practicum.controller.public_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.entity.comment.CommentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentPublicController.class)
class CommentPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private CommentDto sampleCommentDto;

    @BeforeEach
    void setUp() {
        sampleCommentDto = new CommentDto();
        sampleCommentDto.setId(1L);
        sampleCommentDto.setText("Test comment content");
        sampleCommentDto.setAuthorName("User1");
    }

    // --- Тесты для GET /comments/events/{eventId} ---

    @Test
    void getEventComments_WhenValidId_ThenReturn200AndList() throws Exception {
        Long eventId = 1L;
        List<CommentDto> expectedList = List.of(sampleCommentDto);
        when(commentService.getEventComments(eventId)).thenReturn(expectedList);

        mockMvc.perform(get("/comments/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleCommentDto.getId()))
                .andExpect(jsonPath("$[0].text").value(sampleCommentDto.getText()));

        verify(commentService, times(1)).getEventComments(eventId);
    }

    @Test
    void getEventComments_WhenIdIsNegative_ThenReturn400() throws Exception {
        mockMvc.perform(get("/comments/events/{eventId}", -1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).getEventComments(anyLong());
    }

    // --- Тесты для GET /comments/{commentId} ---

    @Test
    void getComment_WhenValidId_ThenReturn200AndDto() throws Exception {
        Long commentId = 1L;
        when(commentService.getComment(commentId)).thenReturn(sampleCommentDto);

        mockMvc.perform(get("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleCommentDto.getId()))
                .andExpect(jsonPath("$.text").value(sampleCommentDto.getText()));

        verify(commentService, times(1)).getComment(commentId);
    }

    @Test
    void getComment_WhenIdIsZero_ThenReturn400() throws Exception {
        mockMvc.perform(get("/comments/{commentId}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).getComment(anyLong());
    }

    // --- Тесты для GET /comments/search ---

    @Test
    void searchComments_WhenValidText_ThenReturn200AndList() throws Exception {
        String searchText = "valid search";
        List<CommentDto> expectedList = List.of(sampleCommentDto);
        Pageable pageable = PageRequest.of(0, 10);
        when(commentService.searchComments(searchText,pageable)).thenReturn(expectedList);

        mockMvc.perform(get("/comments/search")
                        .param("text", searchText)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value(sampleCommentDto.getText()));

        verify(commentService, times(1)).searchComments(searchText,pageable);
    }

    @Test
    void searchComments_WhenTextIsBlank_ThenReturn400() throws Exception {
        mockMvc.perform(get("/comments/search")
                        .param("text", "   ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).searchComments(anyString(),any(Pageable.class));
    }

    @Test
    void searchComments_WhenTextTooShort_ThenReturn400() throws Exception {
        mockMvc.perform(get("/comments/search")
                        .param("text", "a") // Длина 1, ограничение min = 2
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).searchComments(anyString(),any(Pageable.class));
    }

    @Test
    void searchComments_ShouldPassCorrectPageable() throws Exception {
        when(commentService.searchComments(eq("test"), any(Pageable.class)))
                .thenReturn(List.of(sampleCommentDto));

        mockMvc.perform(get("/comments/search")
                        .param("text", "test")
                        .param("from", "20")
                        .param("size", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(commentService).searchComments(eq("test"), captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }
}