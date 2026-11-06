package com.kdt.coffeprince.exception;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler {

    // JSON 타입 불일치 문구 (대상 타입 기준)
    private static final String MSG_BOOLEAN_EXPECTED = "true 또는 false로 입력해 주세요.";
    private static final String MSG_INVALID_FORMAT = "형식이 올바르지 않습니다.";
    private static final String MSG_POSITIVE_INTEGER_EXPECTED = "1 이상의 정수여야 합니다.";

    // 메서드 파라미터 제약
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handle(HandlerMethodValidationException err) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ParameterValidationResult result : err.getParameterValidationResults()) {
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                String field = error instanceof FieldError f
                        ? f.getField()
                        : result.getMethodParameter().getParameterName();
                fieldErrors.putIfAbsent(field, error.getDefaultMessage());
            }
        }
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors));
    }

    //  @Valid @RequestBody 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException err) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : err.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors));
    }

    // @RequestBody JSON 을 읽지 못함
    //  - JSON 문법 오류(StreamReadException): 어느 필드인지 알 수 없으므로 fieldErrors 는 {}
    //  - 타입 불일치(allow-coercion-of-scalars=false 로 "false"·0 → Boolean 변환 거부 등):
    //    Jackson 이 알려주는 필드 경로를 fieldErrors 의 키로 사용
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException err) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (findCause(err, StreamReadException.class) == null) {
            JacksonException cause = findCause(err, JacksonException.class);
            if (cause != null && !cause.getPath().isEmpty()) {
                String field = toFieldPath(cause.getPath());
                if (!field.isEmpty()) {
                    fieldErrors.put(field, typeMismatchMessage(cause));
                }
            }
        }
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors));
    }

    // 경로 ID 가 정수로 변환되지 않음. 예: /menus/abc (날짜 쿼리는 String + @UrlDate 로 받아 여기로 오지 않는다)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException err) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.of(ErrorCode.VALIDATION_ERROR, Map.of(err.getName(), MSG_POSITIVE_INTEGER_EXPECTED)));
    }

    private static String typeMismatchMessage(JacksonException cause) {
        if (cause instanceof MismatchedInputException mismatched) {
            Class<?> target = mismatched.getTargetType();
            if (target == Boolean.class || target == boolean.class) {
                return MSG_BOOLEAN_EXPECTED;
            }
        }
        return MSG_INVALID_FORMAT;
    }

    private static <T extends Throwable> T findCause(Throwable root, Class<T> type) {
        for (Throwable t = root; t != null && t.getCause() != t; t = t.getCause()) {
            if (type.isInstance(t)) return type.cast(t);
        }
        return null;
    }

    // Jackson 경로 [items, [0], menuId] → "items[0].menuId" (Bean Validation 의 FieldError 키와 같은 형식)
    private static String toFieldPath(List<JacksonException.Reference> path) {
        StringBuilder sb = new StringBuilder();
        for (JacksonException.Reference ref : path) {
            if (ref.getPropertyName() != null) {
                if (!sb.isEmpty()) sb.append('.');
                sb.append(ref.getPropertyName());
            } else if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            }
        }
        return sb.toString();
    }

}
