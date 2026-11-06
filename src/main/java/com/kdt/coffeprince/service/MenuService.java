package com.kdt.coffeprince.service;

import com.kdt.coffeprince.config.RedisCacheConfig;
import com.kdt.coffeprince.dto.MenuPageResponse;
import com.kdt.coffeprince.dto.MenuRequest;
import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.repository.CategoryRepository;
import com.kdt.coffeprince.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;


    @Transactional(readOnly = true)
    public MenuResponse one(Long menuId) {
        Menu menu = menuRepository.getOrThrow(menuId);
        return MenuResponse.from(menu);
    }

    // A06 캐시: Redis 키 "menus::all" 에 저장, TTL 동안 DB 재조회 안 함
    @Cacheable(cacheNames = RedisCacheConfig.MENUS, key = "'all'")
    @Transactional(readOnly = true)
    public List<MenuResponse> all() {
        return menuRepository.findAllByOrderByMenuIdAsc()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    @CacheEvict(cacheNames = RedisCacheConfig.MENUS, allEntries = true)
    @Transactional
    public MenuResponse updateMenu(Long menuId, MenuRequest body) {
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

    @Transactional(readOnly = true)
    public List<MenuResponse> available() {
        return menuRepository.findByIsAvailableTrueOrderByMenuIdAsc()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    // A21: 판매 여부 변경
    @CacheEvict(cacheNames = RedisCacheConfig.MENUS, allEntries = true)
    @Transactional
    public MenuResponse changeAvailability(Long menuId, boolean available) {
        Menu menu = this.menuRepository.getOrThrow(menuId);
        menu.changeAvailability(available);
        return MenuResponse.from(menu);
    }

    @CacheEvict(cacheNames = RedisCacheConfig.MENUS, allEntries = true)
    @Transactional
    public MenuResponse createMenu(MenuRequest body) {
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

    @CacheEvict(cacheNames = RedisCacheConfig.MENUS, allEntries = true)
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
        return menuRepository.findByNameContainingOrderByMenuIdAsc(name)
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    // 판매 중지 메뉴 포함, menuId 오름차순 고정. 해당 페이지 행만 DB 에서 가져온다
    @Transactional(readOnly = true)
    public MenuPageResponse list(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("menuId"));
        Page<MenuResponse> found = menuRepository.findPageItems(pageRequest);
        return MenuPageResponse.fromPage(found);
    }
}
