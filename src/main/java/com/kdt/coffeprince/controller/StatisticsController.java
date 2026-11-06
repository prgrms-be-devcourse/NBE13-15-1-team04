package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.CustomerStatistics;
import com.kdt.coffeprince.dto.MenuStatistics;
import com.kdt.coffeprince.dto.OrderStatistics;
import com.kdt.coffeprince.service.StatisticsService;
import com.kdt.coffeprince.util.DateUtils;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/orders")
    public ResponseEntity<OrderStatistics> getOrderStatistics(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) throws BadRequestException {
        LocalDate targetDate = resolveTargetDate(date);
        return ResponseEntity.ok(statisticsService.getOrderStatisticsByDate(targetDate));
    }


    @GetMapping("/menus")
    public ResponseEntity<List<MenuStatistics>> getMenuStatistics(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) throws BadRequestException {
        LocalDate targetDate = resolveTargetDate(date);
        return ResponseEntity.ok(statisticsService.getMenuStatisticsByDate(targetDate));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerStatistics>> getCustomerStatistics(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @PastOrPresent
            LocalDate date
    ) throws BadRequestException {
        LocalDate targetDate = resolveTargetDate(date);
        return ResponseEntity.ok(statisticsService.getCustomerStatisticsByDate(targetDate));
    }

    private static LocalDate resolveTargetDate(LocalDate date) throws BadRequestException {
        return DateUtils.getOrDefault(date);
    }
}
