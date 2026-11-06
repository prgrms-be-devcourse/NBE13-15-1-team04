package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.ReferencePostResponse;
import com.kdt.coffeprince.service.ReferencePostService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 선택 심화: RestClient 외부 게시글 조회 (제공 화면 연결 없음)
// postId 형식 오류(0, 음수, 문자)는 전역 ValidationExceptionHandler가 400 VALIDATION_ERROR로 응답한다
@RestController
@RequestMapping("/api/v1/reference")
@RequiredArgsConstructor
public class ReferencePostController {

    private final ReferencePostService referencePostService;

    @GetMapping("/posts/{postId}")
    public ReferencePostResponse getPost(
            @PathVariable @Positive(message = "게시글 ID는 1 이상의 정수여야 합니다.") Long postId) {
        return referencePostService.getPost(postId);
    }
}
