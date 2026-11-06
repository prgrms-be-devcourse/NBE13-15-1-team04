package com.kdt.coffeprince.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuSearchRequest(
        @NotBlank
        @Size(max = 100, message = "{Size.trimmed}")
        String keyword
) {
    public MenuSearchRequest {
        if (keyword != null) keyword = keyword.trim();
    }
}