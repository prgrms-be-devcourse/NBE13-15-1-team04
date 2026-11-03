package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.dto.PutMenuRequest;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.exception.NotFoundException;
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
        Menu menu = this.menuRepository.findById(menuId).orElseThrow();
        return MenuResponse.from(menu);
    }

    public List<MenuResponse> all() {
        return this.menuRepository.findAll()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, PutMenuRequest body) {
        Menu menu = this.menuRepository.findById(menuId).orElseThrow();

        Category category = this.categoryRepository.findById(body.categoryId()).orElseThrow();

        menu.update(
                category,
                body.name(),
                body.price(),
                body.description()
        );
        return MenuResponse.from(menu);
    }

    public List<MenuResponse> available() {
        return this.menuRepository.findByIsAvailableTrue()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    // A21: 판매 여부 변경
    @Transactional
    public MenuResponse changeAvailability(Long menuId, boolean available) {
        Menu menu = this.menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다."));
        menu.changeAvailability(available);
        return MenuResponse.from(menu);
    }

}
