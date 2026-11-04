package com.kdt.coffeprince.exception;

import com.kdt.coffeprince.controller.CategoryController;
import com.kdt.coffeprince.controller.OrderController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

// Category·Order Controller에서 Spring이 던지는 입력 오류만 명세 형식으로 응답한다 (다른 Controller는 영향 없음)
// 직접 던지는 BusinessException은 BusinessExceptionHandler가 처리한다
// 팀 공통 틀이 정해지면 ApiExceptionAdvice와 합칠 예정
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {CategoryController.class, OrderController.class})
public class CategoryOrderExceptionHandler {

    // 400: @Positive(경로 ID) 또는 @Valid(본문) 검사 실패. 예: /categories/0, 이름 31자
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException err) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ParameterValidationResult result : err.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                // 본문 안의 필드 오류 (예: name)
                for (FieldError error : parameterErrors.getFieldErrors()) {
                    fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
                }
            } else {
                // 경로 변수 오류 (예: categoryId)
                String name = result.getMethodParameter().getParameterName();
                String message = result.getResolvableErrors().isEmpty()
                        ? "값을 확인해 주세요."
                        : result.getResolvableErrors().getFirst().getDefaultMessage();
                fieldErrors.putIfAbsent(name, message);
            }
        }
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors));
    }

    // 400: 경로 ID가 숫자가 아님. 예: /categories/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException err) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.of(ErrorCode.VALIDATION_ERROR, Map.of(err.getName(), "1 이상의 정수여야 합니다.")));
    }

    // 400: JSON 문법 오류 → 명세상 fieldErrors는 {}
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException err) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, Map.of()));
    }
}
