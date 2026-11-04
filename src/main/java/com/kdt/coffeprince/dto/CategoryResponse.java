package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryResponse(Long categoryId, String categoryName) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getCategoryId(), category.getCategoryName());
    }
}
