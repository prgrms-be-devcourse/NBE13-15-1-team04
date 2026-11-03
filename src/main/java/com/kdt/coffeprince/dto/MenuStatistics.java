package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.entity.OrderItem;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record MenuStatistics(
        long menuId,
        String menuName,
        int unitPrice,
        int totalQuantity,
        int totalAmount
) {
    public static List<MenuStatistics> listFrom(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getMenu().getMenuId(),
                        TreeMap::new,
                        Collectors.toList()))
                .values()
                .stream()
                .map(MenuStatistics::from)
                .toList();
    }

    private static MenuStatistics from(List<OrderItem> items) {
        OrderItem first = items.getFirst();
        int totalQuantity = items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        int totalAmount = items.stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        return new MenuStatistics(
                first.getMenu().getMenuId(),
                first.getMenuName(),
                first.getUnitPrice(),
                totalQuantity,
                totalAmount
        );
    }
}