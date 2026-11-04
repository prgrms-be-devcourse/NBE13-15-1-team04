package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CategoryRequest;
import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.dto.CategoryUpdateRequest;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByCategoryIdAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse find(Long categoryId) {
        return CategoryResponse.from(getOrThrow(categoryId));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String categoryName = request.categoryName().trim();
        if (categoryRepository.existsByCategoryName(categoryName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "카테고리 중복");
        }
        Category saved = categoryRepository.saveAndFlush(new Category(categoryName));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long categoryId, CategoryUpdateRequest request) {
        Category category = getOrThrow(categoryId);
        String categoryName = request.categoryName().trim();

        if (categoryRepository.existsByCategoryNameAndCategoryIdNot(categoryName, categoryId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "카테고리 중복");
        }
        category.rename(categoryName);
        categoryRepository.flush();
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        getOrThrow(categoryId);
        if (categoryRepository.countMenusByCategoryId(categoryId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "사용중인 카테고리");
        }
        categoryRepository.deleteCategoryById(categoryId);
    }

    public boolean exists(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    private Category getOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 없음"));
    }
}
