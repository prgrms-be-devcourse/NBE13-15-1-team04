package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Menu;

public record PutMenuRequest(Long categoryId, String name, Integer price, String description) {
}
