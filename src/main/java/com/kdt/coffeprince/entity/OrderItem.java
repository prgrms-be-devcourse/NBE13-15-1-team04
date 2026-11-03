package com.kdt.coffeprince.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // 주문 시점 스냅샷. 이후 Menu 가 바뀌어도 영향 없음
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private Integer quantity;
}
