package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.constant.Regexp;
import com.kdt.coffeprince.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record OrderCreateRequest(
        @NotBlank
        @Pattern(regexp = Regexp.EMAIL)
        @Size(max = Order.MAX_EMAIL_LENGTH)
        String email,

        @NotBlank
        @Size(max = Order.MAX_ADDRESS_LENGTH, message = "{Size.trimmed}")
        String address,

        @NotBlank
        @Pattern(regexp = Regexp.POSTCODE, message = "{Pattern.postcode}")
        String postcode,

        @NotNull
        @Size(min = 1, max = Order.MAX_ITEM_SIZE, message = "{Size.items}")
        @UniqueElements
        @Valid
        List<@NotNull(message = "{NotNull.item}") OrderItemRequest> items
) {
    public OrderCreateRequest {
        if (address != null) {
            address = address.trim();
        }
        if (postcode != null) {
            postcode = postcode.trim();
        }
    }

}
