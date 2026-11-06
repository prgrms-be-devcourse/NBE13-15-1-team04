package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.*;
import com.kdt.coffeprince.service.MenuService;
import com.kdt.coffeprince.validation.IntRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "메뉴 API",
        description = "메뉴 등록,수정,삭제 및 조회 API"
)
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    // 페이징 기본값·상한. 명세: page 기본 0, size 기본 10, size 1~50
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_PAGE_SIZE = "10";
    private static final int MAX_PAGE_SIZE = 50;

    @GetMapping("/{menuId}")
    public MenuResponse getMenu(
            @Positive
            @PathVariable("menuId") Long menuId
    ) {
        return menuService.one(menuId);
    }

    @GetMapping
    public List<MenuResponse> getMenus() {
        return menuService.all();
    }

    @Operation(
            summary = "판매 가능 메뉴 조회",
            description = "현재 판매 가능한 메뉴 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/available")
    public List<MenuResponse> getAvailableMenus() {
        return menuService.available();
    }

    @Operation(
            summary = "메뉴 수정",
            description = "메뉴 ID에 해당하는 메뉴의 카테고리, 이름, 가격, 설명을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "404", description = "메뉴 또는 카테고리를 찾을 수 없음")
    })
    @PutMapping("/{menuId}")
    public MenuResponse updateMenu(
            @Positive @PathVariable("menuId") Long menuId,
            @Valid @RequestBody MenuRequest body
    ) {
        return menuService.updateMenu(menuId, body);
    }

    @PutMapping("/{menuId}/availability")         // A21
    public MenuResponse changeAvailability(
            @PathVariable
            @Positive
            Long menuId,
            @Valid @RequestBody MenuAvailabilityRequest request
    ) {
        return menuService.changeAvailability(menuId, request.available());
    }

    @Operation(
            summary = "메뉴 등록",
            description = "카테고리 ID, 메뉴명, 가격, 설명을 입력하여 새로운 메뉴를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "메뉴 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse createMenu(
            @Valid @RequestBody MenuRequest body
    ) {
        return menuService.createMenu(body);
    }

    @Operation(
            summary = "메뉴 삭제",
            description = "메뉴 ID에 해당하는 메뉴를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "메뉴 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음")
    })
    @DeleteMapping("/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(
            @Positive @PathVariable("menuId") Long menuId
    ) {
        menuService.deleteMenu(menuId);
    }

    @Operation(
            summary = "메뉴 검색",
            description = "메뉴명에 검색어가 포함된 메뉴를 조회합니다. 영문 대소문자를 구분합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public List<MenuResponse> searchMenus(@Valid @ModelAttribute MenuSearchRequest request) {
        return menuService.searchMenus(request.keyword());
    }

    @Operation(
            summary = "메뉴 페이징 조회",
            description = "판매 중지 메뉴를 포함한 전체 메뉴를 ID 오름차순으로 페이지 단위 조회합니다. page 는 0 이상, size 는 1~50 입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "page·size 가 정수가 아니거나 허용 범위를 벗어남")
    })
    @GetMapping("/page")
    public MenuPageResponse getPageMenus(
            @RequestParam(defaultValue = DEFAULT_PAGE) @IntRange(message = "{IntRange.page}") String page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @IntRange(min = 1, max = MAX_PAGE_SIZE) String size
    ) {
        return menuService.list(Integer.parseInt(page), Integer.parseInt(size));
    }
}
