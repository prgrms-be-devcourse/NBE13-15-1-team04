package com.kdt.coffeprince.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "category")
@EntityListeners(AuditingEntityListener.class)
public class Category {

    public static final int MAX_NAME_LENGTH = 30;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // 메뉴가 남아 있는 카테고리는 삭제 불가(ERD: ON DELETE RESTRICT). 삭제 시 메뉴를 같이 지우지 않도록 cascade 없음
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Menu> menus = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }

    public boolean isUsed() {
        return !this.menus.isEmpty();
    }

}
