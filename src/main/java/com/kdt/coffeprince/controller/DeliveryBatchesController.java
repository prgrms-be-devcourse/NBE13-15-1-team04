package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.DeliveryBatchResponse;
import com.kdt.coffeprince.service.OrderService;
import com.kdt.coffeprince.validation.UrlDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-batches")
@RequiredArgsConstructor
public class DeliveryBatchesController {

    private final OrderService orderService;

    @GetMapping                                            // A14
    public List<DeliveryBatchResponse> list(
            @RequestParam
            @UrlDate
            String shippingDate
    ) {
        return orderService.findDeliveryBatches(LocalDate.parse(shippingDate));
    }
}
