package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CategoryRequest;
import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.dto.CategoryUpdateRequest;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.exception.NotFoundException;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.repository.CategoryRepository;
import com.kdt.coffeprince.repository.MenuRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public CategoryResponse find(Long categoryId) {
        return CategoryResponse.from(getOrThrow(categoryId));
    }

    // A15: 카테고리별 메뉴 목록
    public List<MenuResponse> findMenus(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("카테고리를 찾을 수 없습니다.");
        }
        return menuRepository.findAllByCategory_CategoryIdOrderByMenuIdAsc(categoryId).stream()
                .map(MenuResponse::from)
                .toList();
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
