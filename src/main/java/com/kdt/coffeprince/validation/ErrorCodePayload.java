package com.kdt.coffeprince.validation;

import com.kdt.coffeprince.exception.ErrorCode;
import jakarta.validation.Payload;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ErrorCodePayload {
    private ErrorCodePayload() {
    }

    public interface MenuNotFound extends Payload {
    }

    public interface OrderNotFound extends Payload {
    }

    public interface CategoryNotFound extends Payload {
    }

    public interface MenuUnavailable extends Payload {
    }

    private static final Map<Class<? extends Payload>, ErrorCode> MAPPING = Map.of(
            MenuNotFound.class, ErrorCode.MENU_NOT_FOUND,
            OrderNotFound.class, ErrorCode.ORDER_NOT_FOUND,
            CategoryNotFound.class, ErrorCode.CATEGORY_NOT_FOUND,
            MenuUnavailable.class, ErrorCode.MENU_UNAVAILABLE
    );

    public static ErrorCode resolve(Set<Class<? extends Payload>> payloads) {
        return payloads.stream()
                .map(MAPPING::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ErrorCode.VALIDATION_ERROR);
    }
}