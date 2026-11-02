package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/{id}")
    public MenuResponse getMenu(@PathVariable("id") Long id) {
        return this.menuService.one(id);
    }

    @GetMapping
    public List<MenuResponse> getMenus() {
        return this.menuService.all();
    }
}
