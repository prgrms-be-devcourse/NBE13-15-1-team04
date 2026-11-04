package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.entity.OrderItem;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;


/* TODO: 하루에도 금액이 여러 번 변할 수 있음.
    -> unitPrice 필드를 어떻게 담을지 고려할 필요 있음
    1. unitPrice별 별도의 MenuStatistics를 만든다.
    2. 평균 unitPrice를 기록한다
    (totalQuantity * unitPrice(avg) = totalAmount)
*   */
public record MenuStatistics(
        long menuId,
        String menuName,
        int unitPrice,
        int totalQuantity,
        int totalAmount
) {
    public static List<MenuStatistics> listFrom(List<Order> orders) {
        List<Order> placedOrders = Order.filterPlaced(orders);
        return placedOrders.stream()
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