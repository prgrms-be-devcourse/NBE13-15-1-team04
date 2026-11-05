package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A04 카테고리 수정 요청: {"name": "디카페인"}
 */
public record CategoryUpdateRequest(
        @NotBlank
        @Size(max = Category.MAX_NAME_LENGTH, message = "{Size.trimmed}")
        String name
) {
    public CategoryUpdateRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
