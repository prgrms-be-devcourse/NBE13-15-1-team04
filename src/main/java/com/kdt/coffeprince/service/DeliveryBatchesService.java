package com.kdt.coffeprince.service;

import com.kdt.coffeprince.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryBatchesService {
    private final OrderRepository orderRepository;
}
