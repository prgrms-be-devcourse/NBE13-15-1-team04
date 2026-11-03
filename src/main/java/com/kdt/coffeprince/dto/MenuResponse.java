package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Menu;

public record MenuResponse(Long id, Long categoryId, String name, Integer price, String description,
                           Boolean available) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getMenuId(),
                menu.getCategory().getCategoryId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getIsAvailable()
        );
    }
}
