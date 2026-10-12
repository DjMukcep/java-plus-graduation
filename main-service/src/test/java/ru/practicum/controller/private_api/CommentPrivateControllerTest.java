package ru.practicum.controller.private_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.entity.comment.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentPrivateController.class)
class CommentPrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void createComment_whenValid_thenReturns201AndComment() throws Exception {
        Long userId = 1L;
        NewCommentDto newCommentDto = new NewCommentDto("Тестовый текст", 10L);
        CommentDto responseDto = new CommentDto(100L, "Тестовый текст", userId, 10L, "User1", "2026-07-02", null);

        Mockito.when(commentService.createComment(any(NewCommentDto.class), eq(userId)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/users/{userId}/comments", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.text").value("Тестовый текст"))
                .andExpect(jsonPath("$.authorId").value(userId));
    }

    @Test
    void createComment_whenTextIsBlank_thenReturns400BadRequest() throws Exception {
        Long userId = 1L;

        NewCommentDto invalidDto = new NewCommentDto("", 10L);

        mockMvc.perform(post("/users/{userId}/comments", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenUserIdIsNegative_thenReturns400BadRequest() throws Exception {
        Long negativeUserId = -5L;
        NewCommentDto newCommentDto = new NewCommentDto("Валидный текст", 10L);

        mockMvc.perform(post("/users/{userId}/comments", negativeUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_whenValid_thenReturns200AndUpdatedComment() throws Exception {
        Long userId = 1L;
        Long commentId = 100L;
        UpdateCommentDto updateDto = new UpdateCommentDto("Обновленный текст", userId, commentId);
        CommentDto responseDto = new CommentDto(commentId, "Обновленный текст", userId, 10L, "User1", "2026-07-02", "2026-07-02");

        Mockito.when(commentService.updateComment(any(UpdateCommentDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Обновленный текст"))
                .andExpect(jsonPath("$.updated").exists());
    }

    @Test
    void deleteComment_whenValid_thenReturns204NoContent() throws Exception {
        Long userId = 1L;
        Long commentId = 100L;

        Mockito.doNothing().when(commentService).deleteComment(commentId, userId);

        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", userId, commentId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getComments_whenValid_thenReturns200AndList() throws Exception {
        Long userId = 1L;
        CommentDto comment1 = new CommentDto(100L, "Текст 1", userId, 10L, "User1", "2026-07-02", null);
        CommentDto comment2 = new CommentDto(101L, "Текст 2", userId, 10L, "User1", "2026-07-02", null);
        List<CommentDto> expectedList = List.of(comment1, comment2);

        Mockito.when(commentService.getComments(userId))
                .thenReturn(expectedList);

        mockMvc.perform(get("/users/{userId}/comments", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[1].id").value(101));
    }
}