package com.kdt.coffeprince.service;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.repository.MenuRepository;
import com.kdt.coffeprince.repository.OrderItemRepository;
import com.kdt.coffeprince.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        order.changeMemo(memo);
        return OrderResponse.from(order);
    }

    // A16: 주문 취소
    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 이미 취소된 주문: 마감과 관계없이 200, 최초 취소 시각 그대로
        if (order.getStatus() == OrderStatus.CANCELED) {
            return OrderResponse.from(order);
        }
        // DB(DATETIME)가 초 단위라서 초 미만은 버림 (반올림돼서 시각이 달라지는 것 방지)
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        if (!order.isCancellableAt(now)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE);
        }
        order.cancel(now);
        return OrderResponse.from(order);
    }
}
