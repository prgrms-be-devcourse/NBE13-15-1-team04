package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.entity.OrderItem;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record CustomerStatistics
        (
                LocalDate date,
                String email,
                int orderCount,
                int placedOrderCount,
                int cancelledOrderCount,
                int totalQuantity,
                int totalAmount
        ) {

    public static List<CustomerStatistics> listFrom(LocalDate date, List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCustomerEmail,
                        TreeMap::new,
                        Collectors.toList()))
                .values()
                .stream()
                .map(customerOrders -> CustomerStatistics.from(date, customerOrders))
                .toList();
    }

    private static CustomerStatistics from(LocalDate date, List<Order> orders) {
        String email = orders.getFirst().getCustomerEmail();

        List<Order> placedOrders = Order.filterPlaced(orders);

        List<OrderItem> placedItems = placedOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .toList();

        int totalQuantity = placedItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        int totalAmount = placedOrders.stream().mapToInt(Order::getTotalAmount).sum();

        return new CustomerStatistics(
                date,
                email,
                orders.size(),
                placedOrders.size(),
                orders.size() - placedOrders.size(),
                totalQuantity,
                totalAmount
        );
    }
}
