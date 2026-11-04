package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CategoryRequest;
import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.dto.CategoryUpdateRequest;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.exception.NotFoundException;
import com.kdt.coffeprince.repository.CategoryRepository;
import com.kdt.coffeprince.repository.MenuRepository;
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
        String name = request.name().trim();
        if (categoryRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "카테고리 중복");
        }
        Category saved = categoryRepository.saveAndFlush(new Category(name));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long categoryId, CategoryUpdateRequest request) {
        Category category = getOrThrow(categoryId);
        String name = request.name().trim();

        if (categoryRepository.countSameNameExactly(name, categoryId) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }
        category.rename(name);
        categoryRepository.flush();
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        getOrThrow(categoryId);
        if (categoryRepository.countMenusByCategoryId(categoryId) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        categoryRepository.deleteCategoryById(categoryId);
    }

    public boolean exists(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    private Category getOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
