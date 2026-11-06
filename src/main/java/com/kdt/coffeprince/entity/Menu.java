package com.kdt.coffeprince.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Collate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 300;
    public static final int MAX_PRICE = 1_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // A20 메뉴명 검색은 영문 대소문자를 구분. 테이블 기본(utf8mb4_0900_ai_ci)은 대소문자를 무시하므로 이 컬럼만 _cs 지정
    @Collate("utf8mb4_0900_as_cs")
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(length = MAX_DESCRIPTION_LENGTH)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void update(Category category, String name, int price, String description) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public Menu(Category category, String name, Integer price, String description) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.isAvailable = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // A21: 판매 여부만 변경
    public void changeAvailability(boolean available) {
        this.isAvailable = available;
    }

    public boolean isUsed() {
        return !this.orderItems.isEmpty();
    }
}
