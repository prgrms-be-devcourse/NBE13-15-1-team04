package com.kdt.coffeprince.dto;

// A21 판매 여부 변경 요청: {"available": false}
// Boolean 대신 Object로 받는 이유: Boolean이면 "true"(문자열)·1(숫자)이 자동 변환될 수 있어서,
// 일단 받은 뒤 진짜 Boolean인지 Controller에서 직접 확인한다.
public record MenuAvailabilityRequest(Object available) {
}