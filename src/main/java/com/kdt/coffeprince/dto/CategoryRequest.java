package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A03 등록·A04 수정 공용 요청 (필드와 검사 규칙이 같아 하나로 둔다)
public record CategoryRequest(
        @NotBlank @Size(max = Category.MAX_NAME_LENGTH, message = "{Size.trimmed}") String name
) {
    public CategoryRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
