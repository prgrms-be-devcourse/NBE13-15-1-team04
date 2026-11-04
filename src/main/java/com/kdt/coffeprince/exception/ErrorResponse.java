package com.kdt.coffeprince.exception;

import java.util.Map;

// 명세 공통 오류 응답: {"code", "message", "fieldErrors"}
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors) {

    public static ErrorResponse of(ErrorCode errorCode, Map<String, String> fieldErrors) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors);
    }
}
