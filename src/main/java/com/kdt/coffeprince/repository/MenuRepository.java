package com.kdt.coffeprince.repository;


import com.kdt.coffeprince.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
