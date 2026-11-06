package com.kdt.coffeprince.dto;

// 선택 심화: 외부 게시글 조회 응답. 외부 서버(/posts/{postId})가 준 값을 그대로 담는다
public record ReferencePostResponse(Long id, Long userId, String title, String body) {
}
