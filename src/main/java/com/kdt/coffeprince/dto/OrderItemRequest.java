package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.OrderItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

import java.util.Objects;

// A11 주문 접수 요청의 항목 한 줄
public record OrderItemRequest(
        @NotNull
        @Positive
        Long menuId,

        @NotNull
        @Range(min = 1, max = OrderItem.MAX_QUANTITY)
        Integer quantity
) {
    // 주문 항목은 메뉴 ID 로 식별한다 (@UniqueElements 의 menuId 중복 검사용, 수량은 비교하지 않음)
    @Override
    public boolean equals(Object o) {
        return o instanceof OrderItemRequest other && Objects.equals(menuId, other.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(menuId);
    }
}