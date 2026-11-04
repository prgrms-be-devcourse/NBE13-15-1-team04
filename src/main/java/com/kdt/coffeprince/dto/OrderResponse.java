package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.entity.Order;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

// 전체 Order 응답 (A11·A13·A16·A22·A23 공통)
// 명세서 "요청·응답 JSON > 주문 접수·상세·취소·메모·재주문" 기준
public record OrderResponse(
        Long id,
        String email,
        String address,
        String postcode,
        String createdAt,
        String shippingDate,
        Integer totalAmount,
        List<OrderItemResponse> items,
        String status,
        String cancelledAt,
        String memo
) {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    // 예: 2026-11-03T13:00:00+09:00
    private static final DateTimeFormatter SEOUL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerEmail(),
                order.getDeliveryAddress(),
                order.getPostcode(),
                toSeoulTime(order.getOrderedAt()),
                order.getDeliveryDate().toString(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                toResponseStatus(order.getStatus()),
                toSeoulTime(order.getCanceledAt()),
                order.getMemo() == null ? "" : order.getMemo()
        );
    }

    // Instant -> 서울 시각 문자열. 값이 없으면(취소 전 cancelledAt) null
    private static String toSeoulTime(Instant time) {
        return time == null ? null : time.atZone(SEOUL).format(SEOUL_TIME);
    }

    // DB·엔티티는 CANCELED, 명세서 응답은 CANCELLED
    private static String toResponseStatus(OrderStatus status) {
        return status == OrderStatus.CANCELED ? "CANCELLED" : status.name();
    }
}