package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Menu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuSearchRequest(
        @NotBlank
        @Size(max = Menu.MAX_NAME_LENGTH, message = "{Size.trimmed}")
        String keyword
) {
    public MenuSearchRequest {
        if (keyword != null) keyword = keyword.trim();
    }
}