package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmailOrderByOrderedAtDescOrderIdDesc(String customerEmail);

    List<Order> findByCustomerEmailAndDeliveryDateAndStatus(String customerEmail, LocalDate deliveryDate, OrderStatus status);

    List<Order> findByDeliveryDateAndStatus(LocalDate deliveryDate, OrderStatus status);

    @Query("select o from Order o left join fetch o.items where o.orderedAt >= :start and o.orderedAt < :end")
    List<Order> findAllOrderedOn(Instant start, Instant end);
}
