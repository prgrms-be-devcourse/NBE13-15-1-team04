package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CategoryRequest;
import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.repository.CategoryRepository;
import com.kdt.coffeprince.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByCategoryIdAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse find(Long categoryId) {
        return CategoryResponse.from(categoryRepository.getOrThrow(categoryId));
    }

    // A15: 카테고리별 메뉴 목록
    @Transactional(readOnly = true)
    public List<MenuResponse> findMenus(Long categoryId) {
        categoryRepository.getOrThrow(categoryId);
        return menuRepository.findAllByCategory_CategoryIdOrderByMenuIdAsc(categoryId).stream()
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name();

        if (categoryRepository.countByName(name) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }

        Category saved = categoryRepository.saveAndFlush(new Category(name));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.getOrThrow(categoryId);
        String name = request.name();

        if (categoryRepository.countByNameAndCategoryIdNot(name, categoryId) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }

        category.rename(name);
        categoryRepository.flush();
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.getOrThrow(categoryId);

        if (category.isUsed()) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }

        categoryRepository.delete(category);
    }
}
