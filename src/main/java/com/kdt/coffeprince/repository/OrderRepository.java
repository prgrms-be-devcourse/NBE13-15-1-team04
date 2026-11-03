package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
