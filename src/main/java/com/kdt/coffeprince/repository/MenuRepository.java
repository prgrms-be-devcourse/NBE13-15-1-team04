package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByIsAvailableTrue();

    List<Menu> findByNameContaining(String name);

    List<Menu> findAllByCategory_CategoryIdOrderByMenuIdAsc(Long categoryId);

    default Menu getOrThrow(Long menuId) {
        return findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }
}
