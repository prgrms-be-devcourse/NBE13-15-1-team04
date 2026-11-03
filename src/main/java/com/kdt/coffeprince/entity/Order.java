package com.kdt.coffeprince.entity;

import com.kdt.coffeprince.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // 재주문 원본 주문. 최초 주문은 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reorder_source_id", updatable = false)
    private Order reorderSource;

    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    // 우편번호: 숫자 5자리. "01234"처럼 앞의 0을 지키려고 문자열로 저장
    @Column(nullable = false, length = 5)
    private String postcode;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    // items 의 unitPrice * quantity 합계
    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount = 0;

    @CreatedDate
    @Column(name = "ordered_at", nullable = false, updatable = false)
    private Instant orderedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // A22: 메모만 변경. 공백·줄바꿈을 그대로 저장(trim 안 함), ""는 비우기
    public void changeMemo(String memo) {
        this.memo = memo;
    }

}
