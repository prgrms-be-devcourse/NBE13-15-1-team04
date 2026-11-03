package com.kdt.coffeprince.service;

import com.kdt.coffeprince.repository.MenuRepository;
import com.kdt.coffeprince.repository.OrderItemRepository;
import com.kdt.coffeprince.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;
}
