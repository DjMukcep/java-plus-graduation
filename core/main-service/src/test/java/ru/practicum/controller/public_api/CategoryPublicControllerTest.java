package ru.practicum.controller.public_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoriesParamDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.entity.category.CategoryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CategoryPublicController.class)
class CategoryPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void findAll_shouldReturnUsersList() throws Exception {
        List<CategoryDto> categories = List.of(
                CategoryDto.builder()
                        .id(1L)
                        .name("Category 1")
                        .build(),
                CategoryDto.builder()
                        .id(2L)
                        .name("Category 2")
                        .build()
        );

        when(categoryService.findAll(any(CategoriesParamDto.class)))
                .thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Category 1"))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(categoryService).findAll(any(CategoriesParamDto.class));
    }

    @Test
    void findAll_shouldReturnBadRequest_whenFromParamIsNegative() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("from", "-10")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void findAll_shouldReturnBadRequest_whenSizeParamIsZero() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void findById_shouldReturnCategory() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Category 1")
                .build();

        when(categoryService.findById(anyLong()))
                .thenReturn(categoryDto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Category 1"));

        verify(categoryService).findById(1);
    }
}
