package com.kdt.coffeprince.dto;

public record PutMenuRequest(Long categoryId, String menuName, Integer price, String description) {
}
