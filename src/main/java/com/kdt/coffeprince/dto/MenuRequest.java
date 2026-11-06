package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Menu;
import jakarta.validation.constraints.*;

// A08 등록·A09 일반 수정 공용 요청 (네 필드 모두 필수, 검사 규칙 동일)
public record MenuRequest(
        @NotNull
        @Positive
        Long categoryId,

        @NotBlank
        @Size(max = Menu.MAX_NAME_LENGTH, message = "{Size.trimmed}")
        String name,

        @NotNull
        @Positive
        @Max(Menu.MAX_PRICE)
        Integer price,

        @NotNull
        @Size(max = Menu.MAX_DESCRIPTION_LENGTH, message = "{Size.trimmed}")
        String description
) {
    public MenuRequest {
        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = description.trim();
        }
    }
}
