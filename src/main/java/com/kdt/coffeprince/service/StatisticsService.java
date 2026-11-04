package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CustomerStatistics;
import com.kdt.coffeprince.dto.MenuStatistics;
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
        List<Order> orders = this.findOrdersOn(date);
        return OrderStatistics.from(date, orders);
    }

    /* 메뉴 통계의 경우
     * 취소된 요청들은 필터링하고 응답하도록 함.
     * */
    @Transactional(readOnly = true)
    public List<MenuStatistics> getMenuStatisticsByDate(LocalDate date) {
        List<Order> orders = this.findOrdersOn(date);

        return MenuStatistics.listFrom(orders);
    }

    @Transactional(readOnly = true)
    public List<CustomerStatistics> getCustomerStatisticsByDate(LocalDate date) {
        List<Order> orders = this.findOrdersOn(date);
        return CustomerStatistics.listFrom(date, orders);
    }

    private List<Order> findOrdersOn(LocalDate date) {
        Instant start = DateUtils.startOf(date);
        Instant end = DateUtils.startOfNextDay(date);
        return this.orderRepository.findAllOrderedOn(start, end);
    }
}
