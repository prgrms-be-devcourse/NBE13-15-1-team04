package com.kdt.coffeprince.exception;

import com.kdt.coffeprince.validation.ErrorCodePayload;
import jakarta.validation.ConstraintViolation;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handle(HandlerMethodValidationException err) {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (ParameterValidationResult result : err.getParameterValidationResults()) {
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                ConstraintViolation<?> violation = result.unwrap(error, ConstraintViolation.class);
                ErrorCode resolved = ErrorCodePayload.resolve(
                        violation.getConstraintDescriptor().getPayload());
                if (code == ErrorCode.VALIDATION_ERROR) code = resolved;

                String field = error instanceof FieldError f
                        ? f.getField()
                        : result.getMethodParameter().getParameterName();
                fieldErrors.putIfAbsent(field, error.getDefaultMessage());
            }
        }
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, fieldErrors));
    }
}