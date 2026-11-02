package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuResponse one(Long id) {
        Menu menu = this.menuRepository.findById(id).orElseThrow();
        return MenuResponse.from(menu);
    }

    public List<MenuResponse> all() {
        return this.menuRepository.findAll()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }
}
