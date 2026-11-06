package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.CustomerStatistics;
import com.kdt.coffeprince.dto.MenuStatistics;
import com.kdt.coffeprince.dto.OrderStatistics;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.service.StatisticsService;
import com.kdt.coffeprince.util.DateUtils;
import com.kdt.coffeprince.validation.UrlDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/orders")                                 // A17
    public ResponseEntity<OrderStatistics> getOrderStatistics(
            @RequestParam(value = "date", required = false) @UrlDate String date
    ) {
        LocalDate targetDate = resolveTargetDate(date);
        return ResponseEntity.ok(statisticsService.getOrderStatisticsByDate(targetDate));
    }

    @GetMapping("/menus")                                  // A18
    public ResponseEntity<List<MenuStatistics>> getMenuStatistics(
            @RequestParam(value = "date", required = false) @UrlDate String date
    ) {
        LocalDate targetDate = resolveTargetDate(date);
        return ResponseEntity.ok(statisticsService.getMenuStatisticsByDate(targetDate));
    }

    @GetMapping("/customers")                              // A24
    public ResponseEntity<List<CustomerStatistics>> getCustomerStatistics(
            @RequestParam(value = "date", required = false) @UrlDate String date
    ) {
        LocalDate targetDate = resolveTargetDate(date);
        return ResponseEntity.ok(statisticsService.getCustomerStatisticsByDate(targetDate));
    }

    // 날짜가 없으면 오늘. @UrlDate 를 통과한 값이라 parse 는 실패하지 않는다. 미래 날짜는 400 (세 통계 공통)
    private static LocalDate resolveTargetDate(String date) {
        if (date == null) {
            return DateUtils.today();
        }
        LocalDate target = LocalDate.parse(date);
        if (DateUtils.isFuture(target)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, Map.of("date", "오늘 또는 그 이전 날짜여야 합니다."));
        }
        return target;
    }
}