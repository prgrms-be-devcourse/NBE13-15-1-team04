package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.service.DeliveryBatchesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delivery-batches")
@RequiredArgsConstructor
public class DeliveryBatchesController {
    private final DeliveryBatchesService deliveryBatchesService;
}
