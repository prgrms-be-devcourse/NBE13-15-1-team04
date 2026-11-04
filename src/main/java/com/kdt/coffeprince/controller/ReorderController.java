package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.service.ReorderService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// A23 재주문. OrderController와 경로는 같은 /api/v1/orders 아래지만, 충돌을 피하려고 파일을 분리했다
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ReorderController {

    private final ReorderService reorderService;

    @PostMapping("/{orderId}/reorder")             // A23 (요청 본문 없음)
    public ResponseEntity<OrderResponse> reorder(
            @PathVariable @Positive(message = "주문 ID는 1 이상의 정수여야 합니다.") Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reorderService.reorder(orderId));
    }
}
