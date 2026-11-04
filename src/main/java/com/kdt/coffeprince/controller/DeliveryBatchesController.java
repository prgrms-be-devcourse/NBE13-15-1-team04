package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.DeliveryBatchResponse;
import com.kdt.coffeprince.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-batches")
public class DeliveryBatchesController {

    private final OrderService orderService;

    public DeliveryBatchesController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping                                            // A14
    public List<DeliveryBatchResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate) {
        return orderService.findDeliveryBatches(deliveryDate);
    }
}
