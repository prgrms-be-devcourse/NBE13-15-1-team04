package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.service.MenuService;
import com.kdt.coffeprince.dto.PutMenuRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

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

    @GetMapping("/available")
    public List<MenuResponse> getAvailableMenus(){
        return this.menuService.available();
    }

    @PutMapping("/{id}")
    public MenuResponse updateMenu(@PathVariable("id") Long id , @RequestBody PutMenuRequest body){
        return menuService.updateMenu(id,body);

    }
}
