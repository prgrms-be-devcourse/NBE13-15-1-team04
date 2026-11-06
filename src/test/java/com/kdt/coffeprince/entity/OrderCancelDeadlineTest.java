package com.kdt.coffeprince.entity;

import com.kdt.coffeprince.constant.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// A16 취소 마감 경계값 테스트: PC·서버 시계를 바꾸지 않고 "지금 시각"을 직접 넣어서 확인한다.
class OrderCancelDeadlineTest {

    private static Instant seoul(String time) {
        return OffsetDateTime.parse(time + "+09:00").toInstant();
    }

    private static Order placedOrder(LocalDate deliveryDate) {
        Order order = new Order();
        ReflectionTestUtils.setField(order, "deliveryDate", deliveryDate);
        ReflectionTestUtils.setField(order, "status", OrderStatus.PLACED);
        return order;
    }

    @Test
    @DisplayName("배송일 13:59:59에는 취소할 수 있다")
    void cancellableAt135959() {
        Order order = placedOrder(LocalDate.of(2026, 11, 3));
        assertThat(order.isCancellableAt(seoul("2026-11-03T13:59:59"))).isTrue();
    }

    @Test
    @DisplayName("배송일 14:00:00 정각부터는 취소할 수 없다")
    void notCancellableAt140000() {
        Order order = placedOrder(LocalDate.of(2026, 11, 3));
        assertThat(order.isCancellableAt(seoul("2026-11-03T14:00:00"))).isFalse();
        assertThat(order.isCancellableAt(seoul("2026-11-03T14:00:01"))).isFalse();
    }

    @Test
    @DisplayName("배송일 전날에는 시각과 관계없이 취소할 수 있다")
    void cancellableDayBefore() {
        Order order = placedOrder(LocalDate.of(2026, 11, 3));
        assertThat(order.isCancellableAt(seoul("2026-11-02T23:59:59"))).isTrue();
    }

    @Test
    @DisplayName("이미 취소된 주문은 마감 전이어도 다시 취소 대상이 아니다")
    void cancelledOrderIsNotCancellable() {
        Order order = placedOrder(LocalDate.of(2026, 11, 3));
        order.cancel(seoul("2026-11-03T10:00:00"));
        assertThat(order.isCancellableAt(seoul("2026-11-03T11:00:00"))).isFalse();
    }

    @Test
    @DisplayName("취소하면 상태와 취소 시각만 바뀐다")
    void cancelChangesOnlyStatusAndCancelledAt() {
        Order order = placedOrder(LocalDate.of(2026, 11, 3));
        Instant now = seoul("2026-11-03T13:59:59");
        order.cancel(now);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getCanceledAt()).isEqualTo(now);
        assertThat(order.getDeliveryDate()).isEqualTo(LocalDate.of(2026, 11, 3));
    }
}
