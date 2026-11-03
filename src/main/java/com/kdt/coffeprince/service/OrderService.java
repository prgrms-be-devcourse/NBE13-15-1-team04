package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.exception.NotFoundException;
import com.kdt.coffeprince.repository.MenuRepository;
import com.kdt.coffeprince.repository.OrderItemRepository;
import com.kdt.coffeprince.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;

    // A22: 주문 메모 수정 (취소된 주문도 가능, memo만 변경)
    @Transactional
    public OrderResponse changeMemo(Long orderId, String memo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다."));
        order.changeMemo(memo);
        return OrderResponse.from(order);
    }
}
