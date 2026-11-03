package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.exception.NotFoundException;
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

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(String name) {
        Category saved = categoryRepository.save(new Category(name));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, String name) {
        Category category = getCategory(id);
        category.changeName(name);   // save() 없이도 변경 감지로 UPDATE
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategory(id);
        if (menuRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("메뉴가 남아 있는 카테고리는 삭제할 수 없습니다.");
        }
        categoryRepository.delete(category);
    }

    // 다른 서비스에서도 쓸 수 있게 public
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다. id=" + id));
    }
}
