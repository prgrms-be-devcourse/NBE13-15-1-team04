package com.kdt.coffeprince.service;

import com.kdt.coffeprince.constant.OrderStatus;
import com.kdt.coffeprince.dto.DeliveryBatchResponse;
import com.kdt.coffeprince.dto.OrderCreateRequest;
import com.kdt.coffeprince.dto.OrderResponse;
import com.kdt.coffeprince.dto.OrderSummaryResponse;
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

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//OrderService
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final Clock clock;

    private static final LocalTime CUTOFF = LocalTime.of(14, 0, 0);

    // A22: 주문 메모 수정 (취소된 주문도 가능, memo만 변경)
    @Transactional
    public OrderResponse changeMemo(Long orderId, String memo) {
        Order order = orderRepository.getOrThrow(orderId);
        order.changeMemo(memo);
        return OrderResponse.from(order);
    }

    // ---------- A11 주문 접수 ----------
    @Transactional
    public OrderResponse place(OrderCreateRequest request) {
        List<Line> lines = request.items().stream()
                .map(item -> new Line(item.menuId(), item.quantity()))
                .toList();
        Order order = createOrder(request.email(), request.address(), request.postcode(), lines, null);
        return OrderResponse.from(order);
    }

    // ---------- A23 재주문 ----------
    // 원본 주문의 이메일·주소·우편번호·메뉴 ID·수량으로 새 주문을 만든다. 메뉴명·단가는 "현재" DB 값을 쓴다.
    // 취소된 주문도 재주문할 수 있고, 원본 항목은 재사용하지 않으므로 원본 주문은 그대로 남는다.
    @Transactional
    public OrderResponse reorder(Long orderId) {
        Order source = orderRepository.getOrThrow(orderId);
        List<Line> lines = source.getItems().stream()
                .map(item -> new Line(item.getMenuId(), item.getQuantity()))
                .toList();
        Order order = createOrder(source.getCustomerEmail(), source.getDeliveryAddress().trim(),
                source.getPostcode().trim(), lines, source);
        return OrderResponse.from(order);
    }

    // 주문 한 줄: 메뉴 ID + 수량 (A11 요청 항목과 A23 원본 항목의 공통 입력)
    private record Line(Long menuId, int quantity) {
    }

    // A11·A23 공통: 메뉴 상태 검사 → 배송일 계산 → 배송지 검사 → 주문 + 항목 저장 (한 트랜잭션, cascade)
    private Order createOrder(String email, String address, String postcode, List<Line> lines, Order reorderSource) {
        Map<Long, Menu> menus = findAvailableMenus(lines);

        // 접수 시각 기준으로 배송일 계산 (재주문은 처리 시각 기준으로 다시 계산)
        Instant orderedAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        LocalDate shippingDate = calculateShippingDate(orderedAt);
        validateSameAddress(email, shippingDate, address, postcode);

        Order order = Order.place(email, address, postcode, orderedAt, shippingDate, reorderSource);
        for (Line line : lines) {
            order.addItem(new OrderItem(menus.get(line.menuId()), line.quantity()));
        }
        return orderRepository.save(order);
    }

    // 메뉴를 "현재" 상태로 한 번에 조회한다. 없으면 404, 판매 중지면 409
    // (재주문에서 메뉴 없음은 MENU_IN_USE 때문에 보통 생기지 않는다)
    private Map<Long, Menu> findAvailableMenus(List<Line> lines) {
        List<Long> menuIds = lines.stream().map(Line::menuId).distinct().toList();
        Map<Long, Menu> menus = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getMenuId, Function.identity()));

        for (Line line : lines) {
            Menu menu = menus.get(line.menuId());
            if (menu == null) {
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
            if (!Boolean.TRUE.equals(menu.getIsAvailable())) {
                throw new BusinessException(ErrorCode.MENU_UNAVAILABLE);
            }
        }
        return menus;
    }

    // 서울 14:00:00 전이면 당일, 정각부터는 다음 날 (연말이면 다음 해로 넘어감)
    private LocalDate calculateShippingDate(Instant orderedAt) {
        ZonedDateTime seoul = orderedAt.atZone(DateUtils.ZONE);
        return seoul.toLocalTime().isBefore(CUTOFF)
                ? seoul.toLocalDate()
                : seoul.toLocalDate().plusDays(1);
    }

    // 같은 이메일·배송일의 PLACED 주문과 주소·우편번호가 다르면 409
    private void validateSameAddress(String email, LocalDate shippingDate, String address, String postcode) {
        boolean mismatch = orderRepository
                .findByCustomerEmailAndDeliveryDateAndStatus(email, shippingDate, OrderStatus.PLACED)
                .stream()
                .anyMatch(o -> !o.getDeliveryAddress().trim().equals(address)
                        || !o.getPostcode().trim().equals(postcode));
        if (mismatch) {
            throw new BusinessException(ErrorCode.ADDRESS_MISMATCH);
        }
    }


    // ---------- A12 이메일별 주문 ----------
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByOrderedAtDescOrderIdDesc(email).stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    // ---------- A13 주문 상세 ----------
    @Transactional(readOnly = true)
    public OrderResponse find(Long orderId) {
        return OrderResponse.from(orderRepository.getOrThrow(orderId));
    }

    // ---------- A16 주문 취소 ----------
    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = orderRepository.getOrThrow(orderId);
        // 이미 취소된 주문: 마감과 관계없이 200, 최초 취소 시각 그대로
        if (order.getStatus() == OrderStatus.CANCELED) {
            return OrderResponse.from(order);
        }
        // DB(DATETIME)가 초 단위라서 초 미만은 버림 (반올림돼서 시각이 달라지는 것 방지)
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        if (!order.isCancellableAt(now)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE);
        }
        order.cancel(now);
        return OrderResponse.from(order);
    }

    // ---------- A14 배송 묶음 ----------
    @Transactional(readOnly = true)
    public List<DeliveryBatchResponse> findDeliveryBatches(LocalDate shippingDate) {
        List<Order> orders = orderRepository.findByDeliveryDateAndStatus(shippingDate, OrderStatus.PLACED);

        // 이메일 오름차순
        Map<String, List<Order>> byEmail = orders.stream()
                .collect(Collectors.groupingBy(Order::getCustomerEmail, TreeMap::new, Collectors.toList()));

        List<DeliveryBatchResponse> result = new ArrayList<>();
        byEmail.forEach((email, group) -> result.add(toBatch(shippingDate, email, group)));
        return result;
    }

    private DeliveryBatchResponse toBatch(LocalDate shippingDate, String email, List<Order> group) {
        List<Order> sorted = group.stream()
                .sorted(Comparator.comparing(Order::getOrderId))
                .toList();

        List<Long> orderIds = sorted.stream().map(Order::getOrderId).distinct().toList();
        Order first = sorted.getFirst();   // 같은 묶음의 PLACED 주문은 주소가 같음

        // 메뉴 ID·단가·메뉴명이 모두 같을 때만 합산 (정렬: 메뉴 ID → 단가 → 메뉴명)
        Map<ItemKey, Integer> merged = new TreeMap<>(ITEM_KEY_ORDER);
        int totalQuantity = 0;
        int totalAmount = 0;
        for (Order order : sorted) {
            for (OrderItem orderItem : order.getItems()) {
                merged.merge(new ItemKey(orderItem.getMenuId(), orderItem.getUnitPrice(), orderItem.getMenuName()),
                        orderItem.getQuantity(), Integer::sum);
                totalQuantity += orderItem.getQuantity();
                totalAmount += orderItem.getLineAmount();
            }
        }

        List<DeliveryBatchResponse.Item> items = merged.entrySet().stream()
                .map(e -> new DeliveryBatchResponse.Item(
                        e.getKey().menuId(), e.getKey().menuName(), e.getKey().unitPrice(),
                        e.getValue(), e.getKey().unitPrice() * e.getValue()))
                .toList();

        return new DeliveryBatchResponse(shippingDate, email,
                first.getDeliveryAddress(), first.getPostcode(),
                orderIds, orderIds.size(), totalQuantity, totalAmount, items);
    }

    private record ItemKey(Long menuId, int unitPrice, String menuName) {
    }

    private static final Comparator<ItemKey> ITEM_KEY_ORDER =
            Comparator.comparing(ItemKey::menuId, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparingInt(ItemKey::unitPrice)
                    .thenComparing(ItemKey::menuName);

}
