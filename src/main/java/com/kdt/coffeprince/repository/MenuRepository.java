package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByIsAvailableTrue();

    // A20: 메뉴명 검색은 영문 대소문자를 구분 (테이블 기본 콜레이션은 무시하므로 utf8mb4_bin 으로 비교)
    @Query(value = "SELECT * FROM menu WHERE name COLLATE utf8mb4_bin LIKE CONCAT('%', :keyword, '%') ORDER BY menu_id",
            nativeQuery = true)
    List<Menu> searchByNameExactCase(@Param("keyword") String keyword);

    List<Menu> findAllByCategory_CategoryIdOrderByMenuIdAsc(Long categoryId);

    default Menu getOrThrow(Long menuId) {
        return findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }
}
