package com.kdt.coffeprince.exception;

import lombok.Getter;

import java.util.Map;

// 사용법: throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, String> fieldErrors;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, Map.of());
    }

    // 400처럼 어떤 필드가 틀렸는지 알려줘야 할 때
    public BusinessException(ErrorCode errorCode, Map<String, String> fieldErrors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }
}
