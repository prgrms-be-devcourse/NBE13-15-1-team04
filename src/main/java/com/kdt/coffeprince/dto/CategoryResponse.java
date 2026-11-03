package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getCategoryId(), c.getName());
    }
}

