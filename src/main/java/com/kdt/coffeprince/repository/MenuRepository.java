package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    // A06·A19: 명세는 ID 오름차순
    List<Menu> findAllByOrderByMenuIdAsc();

    List<Menu> findByIsAvailableTrueOrderByMenuIdAsc();

    // A20: 메뉴명 부분 일치. Containing 은 검색어의 %·_ 를 이스케이프하므로 일반 문자로 취급된다.
    //      영문 대소문자 구분은 DB 콜레이션(utf8mb4_bin)이 담당한다 (docker-compose.yml·init.sql·README)
    List<Menu> findByNameContainingOrderByMenuIdAsc(String keyword);

    List<Menu> findAllByCategory_CategoryIdOrderByMenuIdAsc(Long categoryId);

    default Menu getOrThrow(Long menuId) {
        return findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }
}
