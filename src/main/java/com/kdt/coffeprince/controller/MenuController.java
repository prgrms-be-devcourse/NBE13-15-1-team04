package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.service.MenuService;
import com.kdt.coffeprince.dto.CreateMenuRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import com.kdt.coffeprince.dto.MenuAvailabilityRequest;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Map;

@Tag(
        name = "메뉴 API",
        description = "메뉴 등록,수정,삭제 및 조회 API"
)
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/{id}")
    public ResponseEntity<MenuResponse> getMenu(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(menuService.one(id));
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenus() {
        return ResponseEntity.ok(menuService.all());
    }

    @Operation(
            summary = "판매 가능 메뉴 조회",
            description = "현재 판매 가능한 메뉴 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/available")
    public ResponseEntity<List<MenuResponse>> getAvailableMenus(){

        return ResponseEntity.ok(menuService.available());
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
    @PutMapping("/{id}")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable("id") Long id ,
            @Valid @RequestBody PutMenuRequest body
    ){
        return ResponseEntity.ok(menuService.updateMenu(id,body));
    }

    @PutMapping("/{menuId}/availability")         // A21
    public ResponseEntity<MenuResponse> changeAvailability(
            @PathVariable
            @Positive(message = "메뉴 ID는 1 이상의 정수여야 합니다.")
            Long menuId,
            @RequestBody MenuAvailabilityRequest request
    ) {
        if (!(request.available() instanceof Boolean available)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    Map.of("available", "판매 여부는 true 또는 false로 입력해 주세요.")
            );
        }
        return ResponseEntity.ok(
                menuService.changeAvailability(menuId,available)
        );
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
    public ResponseEntity<MenuResponse> createMenu(
            @Valid @RequestBody CreateMenuRequest body
    ) {
        MenuResponse response = menuService.createMenu(body);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "메뉴 삭제",
            description = "메뉴 ID에 해당하는 메뉴를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "메뉴 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable("id") Long id
    ) {
        menuService.deleteMenu(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "메뉴 검색",
            description = "메뉴명에 검색어가 포함된 메뉴를 조회합니다. 영문 대소문자를 구분하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<List<MenuResponse>> searchMenus(
            @RequestParam("name") String name
    ) {
        return ResponseEntity.ok(menuService.searchMenus(name));
    }
}
