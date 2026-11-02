package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.MenuResponse;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuResponse one(Long id) {
        Menu menu = this.menuRepository.findById(id).orElseThrow();
        return MenuResponse.from(menu);
    }
}
