package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;

public record CategoryResponse(Long categoryId, String categoryName) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getCategoryId(), category.getCategoryName());
    }
}
