package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Menu;

public record MenuResponse(Long menuId, Long categoryId, String menuName, Integer price,
                           String description, Boolean isAvailable) {

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
