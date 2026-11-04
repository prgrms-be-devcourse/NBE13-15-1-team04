package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByMenu_MenuId(Long menuId);
}
