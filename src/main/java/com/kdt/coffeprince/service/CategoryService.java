package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CategoryRequest;
import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.dto.CategoryUpdateRequest;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.repository.CategoryRepository;
import com.kdt.coffeprince.repository.MenuRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service

public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;

    public CategoryService(CategoryRepository categoryRepository, MenuRepository menuRepository) {
        this.categoryRepository = categoryRepository;
        this.menuRepository = menuRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByCategoryIdAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse find(Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "카테고리 중복");
        }
        Category saved = categoryRepository.saveAndFlush(new Category(name));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = getOrThrow(id);
        String name = request.name().trim();

        if (categoryRepository.existsByNameAndCategoryIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "카테고리 중복");
        }
        category.rename(name);
        categoryRepository.flush();
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        if (categoryRepository.countMenusByCategoryId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "사용중인 카테고리");
        }
        categoryRepository.deleteCategoryById(id);
    }

    public boolean exists(Long id) {
        return categoryRepository.existsById(id);
    }

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 없음"));
    }
}
