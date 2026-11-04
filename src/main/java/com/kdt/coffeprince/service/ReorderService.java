package com.kdt.coffeprince.service;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.entity.OrderItem;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import com.kdt.coffeprince.repository.MenuRepository;
import com.kdt.coffeprince.repository.OrderRepository;
import com.kdt.coffeprince.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// A23 재주문
// 원본 주문의 이메일·주소·우편번호·메뉴 ID·수량으로 새 주문을 만든다. 메뉴명·단가는 "현재" DB 값을 쓴다.
// TODO(리팩토링): 판매 중지 검사·배송일 계산·배송지 검사는 A11(OrderService.place)과 같은 규칙이라 공통 메서드로 합친다.
@Service
@RequiredArgsConstructor
public class ReorderService {

    private static final LocalTime CUTOFF = LocalTime.of(14, 0, 0);

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final Clock clock;

    @Transactional
    public OrderResponse reorder(Long orderId) {
        // 1. 원본 주문 (취소된 주문도 재주문 가능)
        Order source = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 원본 항목의 메뉴를 "현재" 상태로 다시 조회 → 판매 중지면 409
        List<OrderItem> sourceItems = source.getItems();
        Map<Long, Menu> menus = findCurrentMenus(sourceItems);
        for (OrderItem item : sourceItems) {
            Menu menu = menus.get(item.getMenuId());
            if (menu == null) {
                // 주문에 쓰인 메뉴는 삭제할 수 없어서(MENU_IN_USE) 보통은 생기지 않는다
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
            if (!Boolean.TRUE.equals(menu.getIsAvailable())) {
                throw new BusinessException(ErrorCode.MENU_UNAVAILABLE);
            }
        }

        // 3. 접수 시각 = 재주문 처리 시각, 배송일은 이 시각 기준으로 다시 계산
        Instant orderedAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        LocalDate deliveryDate = calculateDeliveryDate(orderedAt);

        // 4. 같은 이메일·배송일의 PLACED 주문과 주소·우편번호가 다르면 409
        String address = source.getDeliveryAddress().trim();
        String postcode = source.getPostcode().trim();
        validateSameDeliveryAddress(source.getCustomerEmail(), deliveryDate, address, postcode);

        // 5. 새 주문 + 새 항목 (원본 항목은 재사용하지 않는다 → 원본 주문은 그대로)
        Order reorder = Order.place(source.getCustomerEmail(), address, postcode,
                orderedAt, deliveryDate, source);
        for (OrderItem item : sourceItems) {
            reorder.addItem(new OrderItem(menus.get(item.getMenuId()), item.getQuantity()));
        }
        return OrderResponse.from(orderRepository.save(reorder));
    }

    private Map<Long, Menu> findCurrentMenus(List<OrderItem> items) {
        List<Long> menuIds = items.stream()
                .map(OrderItem::getMenuId)
                .distinct()
                .toList();
        return menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getMenuId, Function.identity()));
    }

    // 서울 14:00:00 전이면 당일, 정각부터는 다음 날 (연말이면 다음 해로 넘어감)
    private LocalDate calculateDeliveryDate(Instant orderedAt) {
        ZonedDateTime seoul = orderedAt.atZone(DateUtils.ZONE);
        return seoul.toLocalTime().isBefore(CUTOFF)
                ? seoul.toLocalDate()
                : seoul.toLocalDate().plusDays(1);
    }

    private void validateSameDeliveryAddress(String email, LocalDate deliveryDate, String address, String postcode) {
        boolean mismatch = orderRepository
                .findByCustomerEmailAndDeliveryDateAndStatus(email, deliveryDate, OrderStatus.PLACED)
                .stream()
                .anyMatch(o -> !o.getDeliveryAddress().trim().equals(address)
                        || !o.getPostcode().trim().equals(postcode));
        if (mismatch) {
            throw new BusinessException(ErrorCode.ADDRESS_MISMATCH);
        }
    }
}
