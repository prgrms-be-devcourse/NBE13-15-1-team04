package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.OrderItem;

public record OrderItemResponse(
        Long menuId, String menuName, Integer unitPrice, Integer quantity, Long lineAmount) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(orderItem.getMenuId(), orderItem.getMenuName(),
                orderItem.getUnitPrice(), orderItem.getQuantity(), orderItem.getLineAmount());
    }
}
