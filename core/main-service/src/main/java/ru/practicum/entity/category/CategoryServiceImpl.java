package ru.practicum.entity.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoriesParamDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.entity.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto payload) {
        if (categoryRepository.findByName(payload.getName()).isPresent()) {
            throw new ConflictException("This name is already taken");
        }
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.createToEntity(payload)));
    }

    @Override
    @Transactional
    public void delete(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found!"));
        if (eventRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category has related events!");
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto update(long id, CategoryDto payload) {
        categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found!"));
        categoryRepository.findByName(payload.getName()).ifPresent(category -> {
            if (category.getId() != id) {
                throw new ConflictException("This category already exists!");
            }
        });
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.updateToEntity(id, payload)));
    }

    @Override
    public List<CategoryDto> findAll(CategoriesParamDto params) {
        Pageable page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        return categoryRepository.findAll(page).stream().map(CategoryMapper::toDto).toList();
    }

    @Override
    public CategoryDto findById(long id) {
        return CategoryMapper.toDto(categoryRepository
                .findById(id).orElseThrow(() -> new NotFoundException("Category not found!")));
    }
}
