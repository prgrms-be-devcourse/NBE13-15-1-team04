package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.entity.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String customerEmail,
        String deliveryAddress,
        String postcode,
        OffsetDateTime orderedAt,
        LocalDate deliveryDate,
        Long totalAmount,
        List<OrderItemResponse> items,
        OrderStatus status,
        OffsetDateTime canceledAt,
        String memo
) {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerEmail(),
                order.getDeliveryAddress(),
                order.getPostcode(),
                order.getOrderedAt().atZone(SEOUL).toOffsetDateTime(),
                order.getDeliveryDate(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getStatus(),
                order.getCanceledAt() == null ? null : order.getCanceledAt().atZone(SEOUL).toOffsetDateTime(),
                order.getMemo());
    }
}
