package com.kdt.coffeprince.repository;

import com.kdt.coffeprince.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

