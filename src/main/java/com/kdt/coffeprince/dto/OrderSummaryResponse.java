package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Order;

// 이메일별 주문 목록 한 줄 (명세서: id, createdAt, shippingDate, totalAmount, status)
public record OrderSummaryResponse(
        Long id, String createdAt, String shippingDate, Integer totalAmount, String status) {

    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getOrderId(),
                OrderResponse.toSeoulTime(order.getOrderedAt()),
                order.getDeliveryDate().toString(),
                order.getTotalAmount(),
                OrderResponse.toResponseStatus(order.getStatus()));
    }
}
