package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByIdAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    // A05: 이 카테고리에 속한 메뉴 개수 (Menu 엔티티를 거치지 않고 menu 테이블을 직접 센다)
    @Query(value = "SELECT COUNT(*) FROM menu WHERE category_id = :categoryId", nativeQuery = true)
    long countMenusByCategoryId(@Param("categoryId") Long categoryId);

    // A05: 카테고리만 바로 삭제 (cascade로 menu 테이블을 읽지 않도록)
    @Modifying
    @Query("DELETE FROM Category c WHERE c.id = :id")
    int deleteCategoryById(@Param("id") Long id);
}

