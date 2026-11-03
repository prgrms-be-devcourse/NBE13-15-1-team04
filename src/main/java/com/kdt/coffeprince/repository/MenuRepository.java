package com.kdt.coffeprince.repository;


import com.kdt.coffeprince.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByIsAvailableTrue();

    List<Menu> findByNameContainingIgnoreCase(String name);
}
