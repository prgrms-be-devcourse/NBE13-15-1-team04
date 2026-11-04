package com.kdt.coffeprince.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// BusinessException을 명세 형식 {code, message, fieldErrors}로 응답한다
// 모든 Controller에 적용되지만, BusinessException을 직접 던진 API에만 영향이 있다
// (현재 A04·A05·A15·A16·A21·A22). 팀 공통 틀이 정해지면 ApiExceptionAdvice와 합칠 예정
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class BusinessExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException err) {
        ErrorCode errorCode = err.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, err.getFieldErrors()));
    }
}
