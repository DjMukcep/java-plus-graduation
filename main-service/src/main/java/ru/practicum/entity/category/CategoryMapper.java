package ru.practicum.entity.category;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

@UtilityClass
public class CategoryMapper {
    public static Category createToEntity(NewCategoryDto payload) {
        return Category.builder()
                .name(payload.getName())
                .build();
    }

    public static Category updateToEntity(long id, CategoryDto payload) {
        return Category.builder()
                .id(id)
                .name(payload.getName())
                .build();
    }

    public static CategoryDto toDto(Category payload) {
        return CategoryDto.builder()
                .id(payload.getId())
                .name(payload.getName())
                .build();
    }

    public static Category toEntity(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
