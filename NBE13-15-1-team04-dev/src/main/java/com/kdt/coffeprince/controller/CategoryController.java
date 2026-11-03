package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.CategoryResponse;
import com.kdt.coffeprince.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController                        // JSON 응답을 반환하는 컨트롤러
@RequestMapping("/api/v1/categories")  // 이 클래스의 모든 주소 앞에 붙는 공통 경로
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public record CategoryRequest(@NotBlank String name) {}

    // A01 목록 조회: GET /api/v1/categories → 200
    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    // A02 상세 조회: GET /api/v1/categories/{categoryId} → 200
    // @PathVariable: 주소의 {categoryId} 자리 값(예: 1)을 변수로 받음
    @GetMapping("/{categoryId}")
    public CategoryResponse findById(@PathVariable Long categoryId) {
        return categoryService.findById(categoryId);
    }

    // A03 등록: POST /api/v1/categories → 201
    // @RequestBody: 요청 JSON을 CategoryRequest 객체로 변환
    // @Valid: CategoryRequest의 검증 조건(@NotBlank)을 실행, 실패 시 400
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)   // 기본 200 대신 201 Created로 응답
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request.name());
    }

    // A04 수정: PUT /api/v1/categories/{categoryId} → 200
    @PutMapping("/{categoryId}")
    public CategoryResponse update(@PathVariable Long categoryId,
                                   @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(categoryId, request.name());
    }

    // A05 삭제: DELETE /api/v1/categories/{categoryId} → 204 빈 본문
    // 반환 타입이 void이므로 본문이 없고, 상태 코드만 204 No Content로 내려간다.
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
    }
}

