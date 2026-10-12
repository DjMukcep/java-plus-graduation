package ru.practicum.entity.category;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CategoryMapperTest {

    @Test
    void createToEntity_shouldMapCreateDtoToCategoryEntity() {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("Category name")
                .build();

        Category category = CategoryMapper.createToEntity(payload);

        assertEquals("Category name", category.getName());
        assertNull(category.getId());
    }

    @Test
    void updateToEntity_shouldMapUpdateDtoToCategoryEntity() {
        CategoryDto payload = CategoryDto.builder()
                .name("Category name")
                .build();

        Category category = CategoryMapper.updateToEntity(1L, payload);

        assertEquals("Category name", category.getName());
        assertEquals(1L, category.getId());
    }

    @Test
    void toDto_shouldMapCategoryEntityToDto() {
        Category payload = Category.builder()
                .id(1L)
                .name("Category name")
                .build();

        CategoryDto categoryDto = CategoryMapper.toDto(payload);

        assertEquals("Category name", categoryDto.getName());
        assertEquals(1L, categoryDto.getId());
    }
}
