package com.kdt.coffeprince.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// BusinessException(404·409, 그리고 서비스가 직접 던지는 400)을 명세 형식 {code, message, fieldErrors}로 응답한다
// Spring 이 던지는 입력 오류(400)는 ValidationExceptionHandler 가 담당한다
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
