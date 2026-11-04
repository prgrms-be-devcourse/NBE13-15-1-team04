package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.OrderCreateRequest;
import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.dto.OrderSummaryResponse;
import com.kdt.coffeprince.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping                                           // A11
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.place(request);
    }

    @GetMapping                                            // A12
    public List<OrderSummaryResponse> list(@RequestParam(required = false) String customerEmail) {
        return orderService.findByCustomerEmail(customerEmail);
    }

    @GetMapping("/{orderId}")                              // A13
    public OrderResponse get(@PathVariable Long orderId) {
        return orderService.find(orderId);
    }
}
