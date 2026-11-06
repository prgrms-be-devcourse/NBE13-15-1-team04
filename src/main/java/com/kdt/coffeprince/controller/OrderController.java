package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.OrderCreateRequest;
import com.kdt.coffeprince.dto.OrderMemoRequest;
import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.dto.OrderSummaryResponse;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private static final int MEMO_MAX_LENGTH = 200;

    private final OrderService orderService;

    @PostMapping                                           // A11
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.place(request);
    }

    @GetMapping                                            // A12
    public List<OrderSummaryResponse> list(@RequestParam(required = false) String email) {
        return orderService.findByCustomerEmail(email);
    }

    @PutMapping("/{orderId}/memo")                 // A22
    public OrderResponse changeMemo(
            @PathVariable @Positive(message = "주문 ID는 1 이상의 정수여야 합니다.") Long orderId,
            @RequestBody OrderMemoRequest request) {
        if (!(request.memo() instanceof String memo)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, Map.of("memo", "메모는 문자열로 입력해 주세요."));
        }
        // 글자 수: 이모지 같은 문자도 1자로 세기 위해 codePointCount 사용
        if (memo.codePointCount(0, memo.length()) > MEMO_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, Map.of("memo", "메모는 200자 이하로 입력해 주세요."));
        }
        return orderService.changeMemo(orderId, memo);
    }

    @GetMapping("/{orderId}")                              // A13
    public OrderResponse get(@PathVariable Long orderId) {
        return orderService.find(orderId);
    }

    @PostMapping("/{orderId}/cancel")              // A16 (요청 본문 없음)
    public OrderResponse cancel(
            @PathVariable @Positive(message = "주문 ID는 1 이상의 정수여야 합니다.") Long orderId) {
        return orderService.cancel(orderId);
    }

    @PostMapping("/{orderId}/reorder")             // A23 (요청 본문 없음)
    public ResponseEntity<OrderResponse> reorder(
            @PathVariable @Positive(message = "주문 ID는 1 이상의 정수여야 합니다.") Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.reorder(orderId));
    }
}
