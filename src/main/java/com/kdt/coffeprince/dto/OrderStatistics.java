package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.entity.OrderItem;

import java.time.LocalDate;
import java.util.List;

public record OrderStatistics(
        LocalDate date,
        int orderCount,
        int placedOrderCount,
        int cancelledOrderCount,
        int totalQuantity,
        int totalAmount
) {
    public static OrderStatistics from(LocalDate date, List<Order> orders) {
        List<Order> placed = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PLACED)
                .toList();

        int totalQuantity = 0;
        int totalAmount = 0;

        for (Order order : placed) {
            for (OrderItem item : order.getItems()) {
                int quantity = item.getQuantity();
                totalQuantity += quantity;
            }
            totalAmount += order.getTotalAmount();

        }

        return new OrderStatistics(
                date,
                orders.size(),
                placed.size(),
                orders.size() - placed.size(),
                totalQuantity,
                totalAmount
        );
    }
}
