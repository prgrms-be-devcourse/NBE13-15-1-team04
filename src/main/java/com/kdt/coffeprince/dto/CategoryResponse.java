package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getCategoryId(), c.getName());
    }
}

