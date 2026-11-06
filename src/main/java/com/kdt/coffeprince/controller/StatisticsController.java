package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.CustomerStatistics;
import com.kdt.coffeprince.dto.MenuStatistics;
import com.kdt.coffeprince.dto.OrderStatistics;
import com.kdt.coffeprince.service.StatisticsService;
import com.kdt.coffeprince.validation.UrlDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/orders")                                 // A17
    public OrderStatistics getOrderStatistics(
            @RequestParam @UrlDate String date
    ) {
        LocalDate targetDate = resolveTargetDate(date);
        return statisticsService.getOrderStatisticsByDate(targetDate);
    }

    @GetMapping("/menus")                                  // A18
    public List<MenuStatistics> getMenuStatistics(
            @RequestParam @UrlDate String date
    ) {
        LocalDate targetDate = resolveTargetDate(date);
        return statisticsService.getMenuStatisticsByDate(targetDate);
    }

    @GetMapping("/customers")                              // A24
    public List<CustomerStatistics> getCustomerStatistics(
            @RequestParam @UrlDate String date
    ) {
        LocalDate targetDate = resolveTargetDate(date);
        return statisticsService.getCustomerStatisticsByDate(targetDate);
    }

    // date 는 필수(누락 400)이며 @UrlDate 가 형식·실제 날짜·2000~2099 범위를 검사하므로 parse 는 실패하지 않는다.
    // 미래 날짜도 명세 범위 안이면 정상 조회한다 (주문이 없으면 0 객체·빈 배열)
    private static LocalDate resolveTargetDate(String date) {
        return LocalDate.parse(date);
    }
}