package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.OrderStatistics;
import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.repository.OrderRepository;
import com.kdt.coffeprince.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final OrderRepository orderRepository;


    @Transactional(readOnly = true)
    public OrderStatistics getOrderStatisticsByDate(LocalDate date) {
        // TODO: LocalDate, Instant 통일할지 아니면 변환 로직을 유틸리티로 뺄지 결정

        Instant start = date.atStartOfDay(DateUtils.ZONE).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(DateUtils.ZONE).toInstant();
        List<Order> orders = this.orderRepository.findAllOrderedOn(start, end);

        return OrderStatistics.from(date, orders);
    }
}
