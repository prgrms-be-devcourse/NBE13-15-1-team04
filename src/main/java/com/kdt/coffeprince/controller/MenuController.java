package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.service.MenuService;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.dto.CreateMenuRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import com.kdt.coffeprince.dto.MenuAvailabilityRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    @GetMapping("/available")
    public ResponseEntity<List<MenuResponse>> getAvailableMenus(){

        return ResponseEntity.ok(menuService.available());
    }

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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "판매 여부는 true 또는 false로 입력해 주세요."
            );
        }
        return ResponseEntity.ok(
                menuService.changeAvailability(menuId,available)
        );
    }
    @PostMapping
    public ResponseEntity<MenuResponse> createMenu(
            @Valid @RequestBody CreateMenuRequest body
    ) {
        MenuResponse response = menuService.createMenu(body);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable("id") Long id
    ) {
        menuService.deleteMenu(id);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public ResponseEntity<List<MenuResponse>> searchMenus(
            @RequestParam("name") String name
    ) {
        return ResponseEntity.ok(menuService.searchMenus(name));
    }
}
