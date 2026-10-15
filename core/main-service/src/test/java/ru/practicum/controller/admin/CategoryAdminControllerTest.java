package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.entity.category.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CategoryAdminController.class)
class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void create_shouldReturnCreatedCategory() throws Exception {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("Category name")
                .build();

        CategoryDto response = CategoryDto.builder()
                .id(1L)
                .name("Category name")
                .build();

        when(categoryService.create(any(NewCategoryDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Category name"));

        verify(categoryService).create(any(NewCategoryDto.class));
    }

    @Test
    void create_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("")
                .build();


        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void create_shouldReturnBadRequest_whenNameIsBiggerThan50() throws Exception {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("Some loooooooooooooooooooooooooooooooooooooooooooooooooong name")
                .build();


        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).delete(anyLong());
        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1);
    }

    @Test
    void update_shouldReturnUpdatedCategory() throws Exception {
        CategoryDto payload = CategoryDto.builder()
                .name("Category name")
                .build();

        CategoryDto response = CategoryDto.builder()
                .id(1L)
                .name("Category name")
                .build();

        when(categoryService.update(anyLong(), any(CategoryDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Category name"));

        verify(categoryService).update(anyLong(), any(CategoryDto.class));
    }

    @Test
    void update_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        CategoryDto payload = CategoryDto.builder()
                .name("")
                .build();


        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void update_shouldReturnBadRequest_whenNameIsBiggerThan50() throws Exception {
        CategoryDto payload = CategoryDto.builder()
                .name("Some loooooooooooooooooooooooooooooooooooooooooooooooooong name")
                .build();


        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }
}
