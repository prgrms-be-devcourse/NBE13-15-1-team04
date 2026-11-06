package com.kdt.coffeprince.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

// 모든 컨트롤러의 입력 오류(400)를 명세 형식 {code, message, fieldErrors} 로 응답한다
//  - fieldErrors 의 키는 명세의 필드 이름(name·price·items·email·keyword·menuId·orderId·shippingDate·date 등)
//  - 중첩 항목 오류(items[0].quantity)는 최상위 이름(items)으로 묶는다
//  - 같은 키에 오류가 여럿이면 처음 것만 남긴다
//  - 업무 오류(404·409)는 BusinessExceptionHandler 가 처리한다
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler {

    private static final String MSG_REQUIRED = "필수 값입니다.";
    private static final String MSG_POSITIVE_INTEGER_EXPECTED = "1 이상의 정수여야 합니다.";
    // JSON 타입 불일치 문구 (대상 타입 기준)
    private static final String MSG_BOOLEAN_EXPECTED = "true 또는 false로 입력해 주세요.";
    private static final String MSG_INVALID_FORMAT = "형식이 올바르지 않습니다.";

    // 메서드 파라미터 제약 (@PathVariable @Positive, @RequestParam @UrlDate 등) 과 그 안의 @Valid 본문 오류
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException err) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ParameterValidationResult result : err.getParameterValidationResults()) {
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                String field = error instanceof FieldError f
                        ? f.getField()
                        : result.getMethodParameter().getParameterName();
                put(fieldErrors, field, error.getDefaultMessage());
            }
        }
        return badRequest(fieldErrors);
    }

    // @Valid @RequestBody 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException err) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : err.getBindingResult().getFieldErrors()) {
            put(fieldErrors, error.getField(), error.getDefaultMessage());
        }
        return badRequest(fieldErrors);
    }

    // 컨트롤러에 @Validated 가 붙어 있으면 파라미터 제약 위반이 이 예외로 온다.
    // propertyPath "update.categoryId" 의 마지막 이름을 필드명으로 쓴다
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException err) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : err.getConstraintViolations()) {
            String name = "";
            for (Path.Node node : violation.getPropertyPath()) {
                name = node.getName();
            }
            put(fieldErrors, name, violation.getMessage());
        }
        return badRequest(fieldErrors);
    }

    // 필수 쿼리 파라미터 누락. 예: /orders (email 없음), /delivery-batches (shippingDate 없음)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException err) {
        return badRequest(Map.of(err.getParameterName(), MSG_REQUIRED));
    }

    // 경로 ID 가 정수로 변환되지 않음. 예: /menus/abc (날짜 쿼리는 String + @UrlDate 로 받아 여기로 오지 않는다)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException err) {
        return badRequest(Map.of(err.getName(), MSG_POSITIVE_INTEGER_EXPECTED));
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
                    put(fieldErrors, field, typeMismatchMessage(cause));
                }
            }
        }
        return badRequest(fieldErrors);
    }

    private static ResponseEntity<ErrorResponse> badRequest(Map<String, String> fieldErrors) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors));
    }

    // 키를 최상위 필드 이름으로 묶고, 같은 키의 첫 오류만 남긴다
    private static void put(Map<String, String> fieldErrors, String field, String message) {
        fieldErrors.putIfAbsent(rootField(field), message);
    }

    // "items[0].menuId" → "items" (명세는 중첩 항목 오류를 상위 필드 하나로 안내한다)
    private static String rootField(String field) {
        int end = field.length();
        int bracket = field.indexOf('[');
        int dot = field.indexOf('.');
        if (bracket >= 0) end = Math.min(end, bracket);
        if (dot >= 0) end = Math.min(end, dot);
        return field.substring(0, end);
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
