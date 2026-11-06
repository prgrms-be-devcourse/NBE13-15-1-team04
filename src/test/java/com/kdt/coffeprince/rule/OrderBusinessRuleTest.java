package com.kdt.coffeprince.rule;

import com.kdt.coffeprince.dto.OrderRequest;
import com.kdt.coffeprince.dto.OrderItemRequest;
import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.service.OrderService;
import com.kdt.coffeprince.support.MutableClock;
import com.kdt.coffeprince.support.TestClockConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 주문 업무 규칙 자동 테스트 (배송일 계산 / 주문 취소 / 재취소 / 재주문)
// - 시각은 MutableClock 으로 고정해서 14:00 마감 경계를 정확히 검증한다.
// - 메뉴 1번(5,000원)은 db/init.sql 의 초기 데이터를 사용한다.
// - 클래스 전체가 @Transactional 이라 각 테스트 후 DB 변경은 롤백된다.
@SpringBootTest
@Import(TestClockConfig.class)
@Transactional

class OrderBusinessRuleTest {
    //값으 변경 가능
    private static final long MENU_ID = 1L;
    private static final String ADDRESS = "서울시 실습로 10 101호";
    private static final String POSTCODE = "01234";

    @Autowired
    OrderService orderService;

    @Autowired
    MutableClock clock;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManager entityManager;


    //모든 데이터 값은 임의로 변경 가능
    // 시각을 맞춘 뒤 주문을 접수하는 도우미 (시각은 모두 서울 기준)
    private OrderResponse placeAt(int year, int mon, int day, int hour, int min, int sec,
                                  String email, int qty) {
        clock.set(year, mon, day, hour, min, sec);
        return orderService.place(new OrderRequest(
                email, ADDRESS, POSTCODE,
                List.of(new OrderItemRequest(MENU_ID, qty))));
    }

    private static void assertErrorCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(expected);
    }

    // ===================================================== 1. 배송일 계산
    @Nested
    @DisplayName("배송일 계산")
    class DeliveryDateRule {

        @Test
        @DisplayName("14:00:00 이전(13:59:59)은 당일")
        void beforeCutoff() {
            OrderResponse order = placeAt(2030, 5, 10, 13, 59, 59, "rule-dayTest1@test.com", 1);

            assertThat(order.shippingDate()).isEqualTo(LocalDate.of(2030, 5, 10).toString());
        }

        @Test
        @DisplayName("14:00:00 정각부터는 다음 날")
        void atCutoff() {
            OrderResponse order = placeAt(2030, 5, 10, 14, 0, 0, "rule-dayTest2@test.com", 1);

            assertThat(order.shippingDate()).isEqualTo(LocalDate.of(2030, 5, 11).toString());
        }

        @Test
        @DisplayName("14:00:01은 다음 날")
        void afterCutoff() {
            OrderResponse order = placeAt(2030, 5, 10, 14, 0, 1, "rule-dayTest3@test.com", 1);

            assertThat(order.shippingDate()).isEqualTo(LocalDate.of(2030, 5, 11).toString());
        }

        @Test
        @DisplayName("12월 31일 14:00:00 이후는 다음 해 1월 1일")
        void yearEnd() {
            OrderResponse order = placeAt(2030, 12, 31, 14, 0, 0, "rule-dayTest4@test.com", 1);

            assertThat(order.shippingDate()).isEqualTo(LocalDate.of(2031, 1, 1).toString());
        }

        @Test
        @DisplayName("응답 createdAt 은 저장 시점이 아니라 배송일 계산에 쓴 접수 시각 그대로다")
        void createdAtUsesRequestInstant() {
            OrderResponse order = placeAt(2030, 5, 10, 13, 59, 59, "rule-dayTest5@test.com", 1);

            assertThat(order.createdAt()).isEqualTo("2030-05-10T13:59:59+09:00");
            assertThat(order.shippingDate()).isEqualTo(LocalDate.of(2030, 5, 10).toString());
        }
    }

    // ===================================================== 2. 주문 취소
    @Nested
    @DisplayName("주문 취소")
    class CancelRule {

        @Test
        @DisplayName("배송일 14:00:00 이전(13:59:59)에는 취소 성공")
        void beforeDeadline() {
            OrderResponse order = placeAt(2030, 5, 10, 10, 0, 0, "rule-cancelTest1@test.com", 2);

            clock.set(2030, 5, 10, 13, 59, 59);
            OrderResponse cancelled = orderService.cancel(order.id());

            assertThat(cancelled.status()).isEqualTo("주문취소");
            assertThat(cancelled.cancelledAt()).isNotNull();
            assertThat(cancelled.totalAmount()).isEqualTo(order.totalAmount());
            assertThat(cancelled.items()).hasSameSizeAs(order.items());
        }

        @Test
        @DisplayName("배송일 14:00:00 정각에는 취소 불가")
        void atDeadline() {
            OrderResponse order = placeAt(2030, 5, 10, 10, 0, 0, "rule-cancelTest2@test.com", 2);

            clock.set(2030, 5, 10, 14, 0, 0);

            assertErrorCode(() -> orderService.cancel(order.id()), ErrorCode.ORDER_NOT_CANCELLABLE);
        }

        @Test
        @DisplayName("마감 기준은 접수일이 아니라 배송일 (5/10 15:00 접수 → 배송일 5/11 → 5/11 13:59:59까지 취소 가능)")
        void deadlineUsesDeliveryDate() {
            OrderResponse order = placeAt(2030, 5, 10, 15, 0, 0, "rule-cancelTest3@test.com", 1);
            assertThat(order.shippingDate()).isEqualTo(LocalDate.of(2030, 5, 11).toString());

            clock.set(2030, 5, 11, 13, 59, 59);

            assertThat(orderService.cancel(order.id()).status()).isEqualTo("주문취소");
        }
    }

    // ===================================================== 3. 재취소
    @Nested
    @DisplayName("재취소")
    class CancelAgainRule {

        @Test
        @DisplayName("마감 이후에도 성공하고, 최초 취소 시각과 주문 값은 그대로")
        void cancelAgainAfterDeadline() {
            OrderResponse order = placeAt(2030, 5, 10, 10, 0, 0, "rule-reCancelTest1@test.com", 2);

            clock.set(2030, 5, 10, 12, 0, 0);
            OrderResponse first = orderService.cancel(order.id());

            clock.set(2030, 5, 10, 15, 0, 0); // 마감(14:00) 이후
            OrderResponse again = orderService.cancel(order.id()); // 예외 없이 성공해야 한다

            assertThat(again.status()).isEqualTo("주문 취소");
            assertThat(again.cancelledAt()).isEqualTo(first.cancelledAt());
            assertThat(again.totalAmount()).isEqualTo(order.totalAmount());
            assertThat(again.items()).hasSameSizeAs(order.items());
            assertThat(again.items().get(0).unitPrice()).isEqualTo(order.items().get(0).unitPrice());
        }
    }

    // ===================================================== 4. 재주문
    @Nested
    @DisplayName("재주문")
    class ReorderRule {

        @Test
        @DisplayName("현재 가격으로 새 주문을 만들고, 원본의 단가·항목 금액·총액은 보존")
        void reorderUsesCurrentPrice() {
            OrderResponse original = placeAt(2030, 5, 10, 10, 0, 0, "rule-reOrderTest1@test.com", 2); // 5,000 × 2
            assertThat(original.items().get(0).unitPrice()).isEqualTo(5000);
            assertThat(original.totalAmount()).isEqualTo(10000);

            // 가격 인상 (JDBC 로 바꾼 뒤 JPA 1차 캐시를 비워야 새 값이 읽힌다)
            jdbc.update("update menu set price = 6000 where menu_id = ?", MENU_ID);
            entityManager.flush();
            entityManager.clear();

            clock.set(2030, 5, 10, 11, 0, 0);
            OrderResponse reorder = orderService.reorder(original.id());

            // 새 주문은 현재 가격
            assertThat(reorder.id()).isNotEqualTo(original.id());
            assertThat(reorder.items().get(0).unitPrice()).isEqualTo(6000);
            assertThat(reorder.items().get(0).lineAmount()).isEqualTo(12000);
            assertThat(reorder.totalAmount()).isEqualTo(12000);

            // 원본은 그대로
            OrderResponse after = orderService.find(original.id());
            assertThat(after.items().get(0).unitPrice()).isEqualTo(5000);
            assertThat(after.items().get(0).lineAmount()).isEqualTo(10000);
            assertThat(after.totalAmount()).isEqualTo(10000);
        }
    }
}
