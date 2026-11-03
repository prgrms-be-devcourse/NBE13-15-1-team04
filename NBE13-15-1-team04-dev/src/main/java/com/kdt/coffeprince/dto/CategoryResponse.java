package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;

public record CategoryResponse(String categoryName, Long categoryId) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getName(), category.getId());
    }
}
