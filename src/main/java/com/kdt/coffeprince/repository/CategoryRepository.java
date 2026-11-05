package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByCategoryIdAsc();

    // A03: 대소문자까지 똑같은 이름의 개수 (테이블 기본 콜레이션은 Decaf와 decaf를 같게 보므로 utf8mb4_bin 으로 비교)
    @Query(value = "SELECT COUNT(*) FROM category WHERE name COLLATE utf8mb4_bin = :name",
            nativeQuery = true)
    long countSameNameExactly(@Param("name") String name);

    // A04: 위와 같되 자기 자신은 제외
    @Query(value = "SELECT COUNT(*) FROM category WHERE name COLLATE utf8mb4_bin = :name AND category_id <> :id",
            nativeQuery = true)
    long countSameNameExactly(@Param("name") String name, @Param("id") Long id);

    // A05: 카테고리만 바로 삭제 (cascade로 menu 테이블을 읽지 않도록)
    @Modifying
    @Query("DELETE FROM Category c WHERE c.categoryId = :id")
    int deleteCategoryById(@Param("id") Long id);

    default Category getOrThrow(Long categoryId) {
        return findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}

