package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.entity.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record OrderSummaryResponse(
        Long orderId, OffsetDateTime orderedAt, LocalDate deliveryDate, Integer totalAmount, OrderStatus status) {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getOrderId(),
                order.getOrderedAt().atZone(SEOUL).toOffsetDateTime(),
                order.getDeliveryDate(),
                order.getTotalAmount(),
                order.getStatus());
    }
}
