package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.OrderStatistics;
import com.kdt.coffeprince.service.StatisticsService;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/orders")
    public ResponseEntity<OrderStatistics> getOrderStatistics(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @PastOrPresent
            LocalDate date
    ) {
        return ResponseEntity.ok(statisticsService.getOrderStatisticsByDate(date));
    }
}
