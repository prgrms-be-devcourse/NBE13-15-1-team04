package com.kdt.coffeprince.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrderCreateRequest(
        @NotBlank @Email String email,
        @NotBlank String address,
        @NotBlank String postcode,
        @NotEmpty @Valid List<Item> items
) {

    public record Item(
            @NotNull @Positive Long menuId,
            @NotNull @Positive Integer quantity
    ) {}
}
