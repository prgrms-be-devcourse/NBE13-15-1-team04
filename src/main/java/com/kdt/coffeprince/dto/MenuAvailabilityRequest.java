package com.kdt.coffeprince.dto;

import jakarta.validation.constraints.NotNull;

// A21 판매 여부 변경 요청: {"available": false}
// "true"(문자열)·1(숫자) 같은 스칼라 강제 변환은 application.yaml 의
// spring.jackson.mapper.allow-coercion-of-scalars=false 로 막아서 진짜 boolean 만 받도록 수정
public record MenuAvailabilityRequest(
        @NotNull(message = "{NotNull.onlyTrueFalse}")
        Boolean available
) {
}
