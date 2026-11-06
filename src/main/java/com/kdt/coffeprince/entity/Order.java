package com.kdt.coffeprince.entity;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.util.DateUtils;
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

    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_ADDRESS_LENGTH = 200;
    public static final int MAX_ITEM_SIZE = 10;
    public static final int MAX_MEMO_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // 재주문 원본 주문. 최초 주문은 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reorder_source_id", updatable = false)
    private Order reorderSource;

    @Column(name = "customer_email", nullable = false, length = MAX_EMAIL_LENGTH)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "delivery_address", nullable = false, length = MAX_ADDRESS_LENGTH)
    private String deliveryAddress;

    // 우편번호: 숫자 5자리. "01234"처럼 앞의 0을 지키려고 문자열로 저장
    @Column(nullable = false, length = 5)
    private String postcode;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(length = MAX_MEMO_LENGTH)
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
    @OrderBy("orderItemId ASC")
    private List<OrderItem> items = new ArrayList<>();

    // 주문 생성: 접수 시각과 배송일은 서비스가 계산해서 넘김
    public static Order place(String customerEmail, String deliveryAddress, String postcode,
                              Instant orderedAt, LocalDate deliveryDate, Order reorderSource) {
        Order o = new Order();
        o.customerEmail = customerEmail;
        o.deliveryAddress = deliveryAddress;
        o.postcode = postcode;
        o.orderedAt = orderedAt;
        o.deliveryDate = deliveryDate;
        o.reorderSource = reorderSource;
        o.status = OrderStatus.PLACED;
        o.canceledAt = null;
        o.memo = "";
        return o;
    }

    public boolean isPlaced() {
        return this.status == OrderStatus.PLACED;
    }

    public static List<Order> filterPlaced(List<Order> orders) {
        return orders.stream()
                .filter(Order::isPlaced)
                .toList();
    }

    // A22: 메모만 변경. 공백·줄바꿈을 그대로 저장(trim 안 함), ""는 비우기
    public void changeMemo(String memo) {
        this.memo = memo;
    }

    // A16: 취소 마감 = 저장된 배송일의 서울 시각 14:00:00. 13:59:59까지의 PLACED 주문만 취소 가능
    public boolean isCancellableAt(Instant now) {
        Instant deadline = deliveryDate.atTime(14, 0).atZone(DateUtils.ZONE).toInstant();
        return isPlaced() && now.isBefore(deadline);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        this.totalAmount += item.getUnitPrice() * item.getQuantity();
    }

    // A16: 상태와 취소 시각만 변경 (주문·항목·금액은 그대로)
    public void cancel(Instant now) {
        this.status = OrderStatus.CANCELED;
        this.canceledAt = now;
    }

}
