package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/{menuId}")
    public MenuResponse getMenu(@PathVariable Long menuId) {
        return menuService.one(menuId);
    }

    @GetMapping
    public List<MenuResponse> getMenus() {
        return menuService.all();
    }

    @GetMapping("/available")
    public List<MenuResponse> getAvailableMenus() {
        return menuService.available();
    }

    @PutMapping("/{menuId}")
    public MenuResponse updateMenu(@PathVariable Long menuId, @RequestBody PutMenuRequest request) {
        return menuService.updateMenu(menuId, request);
    }
}
