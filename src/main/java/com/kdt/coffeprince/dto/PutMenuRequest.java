package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Menu;
import jakarta.validation.constraints.*;

public record PutMenuRequest(
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
    public PutMenuRequest {
        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = description.trim();
        }
    }
}