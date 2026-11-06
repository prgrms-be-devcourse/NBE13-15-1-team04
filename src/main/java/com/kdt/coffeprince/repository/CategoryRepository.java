package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByCategoryIdAsc();

    // A03: 같은 이름의 개수. 대소문자 구분은 DB 콜레이션(utf8mb4_bin)이 담당한다 (docker-compose.yml·init.sql·README)
    long countByName(String name);

    // A04: 위와 같되 자기 자신은 제외
    long countByNameAndCategoryIdNot(String name, Long categoryId);

    default Category getOrThrow(Long categoryId) {
        return findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}

