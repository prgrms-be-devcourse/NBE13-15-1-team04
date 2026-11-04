package com.kdt.coffeprince.dto;

import java.time.LocalDate;
import java.util.List;

public record DeliveryBatchResponse(
        LocalDate deliveryDate,
        String customerEmail,
        String deliveryAddress,
        String postcode,
        List<Long> orderIds,
        Integer orderCount,
        Integer totalQuantity,
        Long totalAmount,
        List<Item> items
) {
    public record Item(Long menuId, String menuName, Integer unitPrice, Integer quantity, Long lineAmount) {}
}
