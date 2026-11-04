package com.kdt.coffeprince.entity;

import com.kdt.coffeprince.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reorder_source_id", updatable = false)
    private Order reorderSource;

    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @Column(name = "customer_name", length = 50)      // nullable = false 제거
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "post_code", nullable = false)
    private String postcode;

    @Column(nullable = false)
    private LocalDate createdAt;                      // 서울 기준 접수일

    @Column
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount = 0L;

    @Column(name = "ordered_at", nullable = false, updatable = false)   // @CreatedDate 제거
    private Instant orderedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderItemId ASC")                       // OrderItem의 PK 필드명에 맞추세요
    private List<OrderItem> items = new ArrayList<>();

    // 주문 생성: 접수 시각과 배송일은 서비스가 계산해서 넘김
    public static Order place(String customerEmail, String deliveryAddress, String postcode,
                              Instant orderedAt, LocalDate deliveryDate, Order reorderSource) {
        Order o = new Order();
        o.customerEmail = customerEmail;
        o.deliveryAddress = deliveryAddress;
        o.postcode = postcode;
        o.orderedAt = orderedAt;
        o.createdAt = orderedAt.atZone(SEOUL).toLocalDate();
        o.deliveryDate = deliveryDate;
        o.reorderSource = reorderSource;
        o.status = OrderStatus.PLACED;
        o.canceledAt = null;
        o.memo = "";
        return o;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        this.totalAmount += item.getLineAmount();
    }

}
