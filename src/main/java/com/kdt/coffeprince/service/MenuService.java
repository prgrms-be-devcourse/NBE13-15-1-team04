package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CreateMenuRequest;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.repository.CategoryRepository;
import com.kdt.coffeprince.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;

    public MenuResponse one(Long menuId) {
        Menu menu = menuRepository.findById(menuId).orElseThrow();
        return MenuResponse.from(menu);
    }

    public List<MenuResponse> all() {
        return menuRepository.findAll()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, PutMenuRequest body) {
        Menu menu = menuRepository.findById(menuId).orElseThrow();

        Category category = categoryRepository.findById(body.categoryId())
                .orElseThrow();

        menu.update(
                category,
                body.name(),
                body.price(),
                body.description()
        );

        return MenuResponse.from(menu);
    }

    public List<MenuResponse> available() {
        return menuRepository.findByIsAvailableTrue()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional
    public MenuResponse createMenu(CreateMenuRequest body) {
        Category category = categoryRepository.findById(body.categoryId())
                .orElseThrow();

        Menu menu = new Menu(
                category,
                body.name(),
                body.price(),
                body.description()
        );

        Menu savedMenu = menuRepository.save(menu);

        return MenuResponse.from(savedMenu);
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow();

        menuRepository.delete(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> searchMenus(String name) {
        return menuRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(MenuResponse::from)
                .toList();
    }
}