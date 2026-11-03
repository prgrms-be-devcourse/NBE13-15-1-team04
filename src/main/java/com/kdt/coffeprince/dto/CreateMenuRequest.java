package com.kdt.coffeprince.dto;

public record CreateMenuRequest(
        Long categoryId,
        String name,
        Integer price,
        String description
) {
}
