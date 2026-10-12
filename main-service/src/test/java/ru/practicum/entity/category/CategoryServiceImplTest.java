package ru.practicum.entity.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.category.CategoriesParamDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.entity.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void create_shouldSaveCategory() {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("Test Event")
                .build();

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.create(payload);

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_shouldThrowException_whenCategoryWithNameExists() {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("Test Event")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Test Event")
                .build();

        when(categoryRepository.findByName(any(String.class)))
                .thenReturn(Optional.ofNullable(category));

        assertThatThrownBy(() -> categoryService.create(payload))
                .isInstanceOf(ConflictException.class)
                        .hasMessage("This name is already taken");

        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void delete_shouldDeleteCategory() {
        Category category = Category.builder()
                .id(1L)
                .name("Event name")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_shouldThrowException_whenCategoryIsNotFound() {
        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found!");
    }

    @Test
    void update_shouldSaveCategory() {
        CategoryDto payload = CategoryDto.builder()
                .name("Test Event")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Event name")
                .build();

        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(category));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.update(1, payload);

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_shouldThrowException_whenCategoryNotFoundById() {
        CategoryDto payload = CategoryDto.builder()
                .name("Test Event")
                .build();

        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> categoryService.update(1, payload))
                .isInstanceOf(NotFoundException.class)
                        .hasMessage("Category not found!");

        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void update_shouldThrowException_whenCategoryWithNameExists() {
        CategoryDto payload = CategoryDto.builder()
                .name("Event name 2")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Event name 1")
                .build();

        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(category));

        when(categoryRepository.findByName(any(String.class)))
                .thenReturn(Optional.ofNullable(category));


        assertThatThrownBy(() -> categoryService.update(2, payload))
                .isInstanceOf(ConflictException.class)
                .hasMessage("This category already exists!");

        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void findAll_shouldReturnCategories() {
        CategoriesParamDto params = CategoriesParamDto.builder()
                .from(0)
                .size(10)
                .build();

        List<Category> categories = List.of(
                Category.builder().id(1L).name("Category 1").build(),
                Category.builder().id(2L).name("Category 2").build()
        );

        when(categoryRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(categories));

        List<CategoryDto> result = categoryService.findAll(params);

        assertEquals(2, result.size());

        verify(categoryRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void findById_shouldReturnCategory() {
        Category category = Category.builder().id(1L).name("Category 1").build();

        when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(category));

        CategoryDto result = categoryService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Category 1", result.getName());

        verify(categoryRepository).findById(1L);
    }
}
