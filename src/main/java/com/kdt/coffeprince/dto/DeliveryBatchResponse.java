package com.kdt.coffeprince.dto;

import java.time.LocalDate;
import java.util.List;

public record DeliveryBatchResponse(
        LocalDate shippingDate,
        String email,
        String address,
        String postcode,
        List<Long> orderIds,
        Integer orderCount,
        Integer totalQuantity,
        Integer totalAmount,
        List<Item> items
) {
    public record Item(Long menuId, String menuName, Integer unitPrice, Integer quantity, Integer lineAmount) {
    }
}
