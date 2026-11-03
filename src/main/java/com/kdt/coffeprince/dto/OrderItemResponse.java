package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.OrderItem;

// 전체 Order 응답의 items 한 줄
// lineAmount = 단가 × 수량 (주문 당시 단가 기준)
public record OrderItemResponse(
        Long menuId,
        String menuName,
        Integer unitPrice,
        Integer quantity,
        Integer lineAmount
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getMenu() == null ? null : item.getMenu().getMenuId(),
                item.getMenuName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice() * item.getQuantity()
        );
    }
}