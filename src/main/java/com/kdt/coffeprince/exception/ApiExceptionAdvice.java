package com.kdt.coffeprince.exception;

import com.kdt.coffeprince.dto.ApiError;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionAdvice {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException err) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", err.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException err) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("BAD_REQUEST", err.getMessage()));
    }
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiError> handleValidation(
//            MethodArgumentNotValidException err
//    ) {
//        String message = err.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .findFirst()
//                .map(fieldError -> fieldError.getDefaultMessage())
//                .orElse("잘못된 요청입니다.");
//
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(new ApiError("BAD_REQUEST", message));
//    }
}
