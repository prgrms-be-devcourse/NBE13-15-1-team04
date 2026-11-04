package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmailOrderByOrderedAtDescOrderIdDesc(String customerEmail);
    
    List<Order> findByCustomerEmailAndDeliveryDateAndStatus(String customerEmail, LocalDate deliveryDate, OrderStatus status);

    List<Order> findByDeliveryDateAndStatus(LocalDate deliveryDate, OrderStatus status);
}
