package com.kdt.coffeprince.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record MenuPageResponse(
        List<MenuResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
    public static MenuPageResponse fromPage(Page<MenuResponse> page) {
        return new MenuPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}