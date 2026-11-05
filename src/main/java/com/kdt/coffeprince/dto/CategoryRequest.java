package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = Category.MAX_NAME_LENGTH, message = "{Size.trimmed}") String name
) {
    public CategoryRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
