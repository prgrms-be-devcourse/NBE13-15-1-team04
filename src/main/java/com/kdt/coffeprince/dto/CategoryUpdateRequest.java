package com.kdt.coffeprince.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A04 카테고리 수정 요청: {"name": "디카페인"}
 */
public record CategoryUpdateRequest(
        @NotBlank(message = "카테고리명을 입력해 주세요.")
        @Size(min = 1, max = 30, message = "카테고리명은 앞뒤 공백을 제외하고 1~30자여야 합니다.")
        String name
) {
    // 검사(@NotBlank, @Size)보다 먼저 실행된다 → 앞뒤 공백을 지운 뒤 길이를 검사하게 된다
    public CategoryUpdateRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
