package com.kdt.coffeprince.dto;

// A22 주문 메모 수정 요청: {"memo": "주문 확인 완료"} / 비우기: {"memo": ""}
// String 대신 Object로 받는 이유: String이면 123(숫자)·true 등이 문자열로 자동 변환될 수 있어서,
// 일단 받은 뒤 진짜 문자열인지 Controller에서 직접 확인한다.
public record OrderMemoRequest(Object memo) {
}
