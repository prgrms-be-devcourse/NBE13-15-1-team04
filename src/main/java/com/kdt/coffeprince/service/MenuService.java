package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.CreateMenuRequest;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
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
        Menu menu = menuRepository.getOrThrow(menuId);
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
        Menu menu = menuRepository.getOrThrow(menuId);
        Category category = categoryRepository.getOrThrow(body.categoryId());

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

    // A21: 판매 여부 변경
    @Transactional
    public MenuResponse changeAvailability(Long menuId, boolean available) {
        Menu menu = this.menuRepository.getOrThrow(menuId);
        menu.changeAvailability(available);
        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse createMenu(CreateMenuRequest body) {
        Category category = this.categoryRepository.getOrThrow(body.categoryId());

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
        Menu menu = this.menuRepository.getOrThrow(menuId);
        if (menu.isUsed()) {
            throw new BusinessException(ErrorCode.MENU_IN_USE);
        }
        menuRepository.delete(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> searchMenus(String name) {
        return menuRepository.findByNameContaining(name)
                .stream()
                .map(MenuResponse::from)
                .toList();
    }
}
