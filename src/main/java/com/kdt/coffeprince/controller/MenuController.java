package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PutMapping("/{menuId}/availability")         // A21
    public MenuResponse changeAvailability(
            @PathVariable @Positive(message = "메뉴 ID는 1 이상의 정수여야 합니다.") Long menuId,
            @RequestBody MenuAvailabilityRequest request) {
        if (!(request.available() instanceof Boolean available)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "판매 여부는 true 또는 false로 입력해 주세요.");
        }
        return menuService.changeAvailability(menuId, available);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse createMenu(@RequestBody CreateMenuRequest body) {
        return menuService.createMenu(body);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable("id") Long id) {
        menuService.deleteMenu(id);
    }
    @GetMapping("/search")
    public List<MenuResponse> searchMenus(@RequestParam("name") String name) {
        return menuService.searchMenus(name);
    }
}
