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
import com.kdt.coffeprince.exception.NotFoundException;
import com.kdt.coffeprince.repository.MenuRepository;
import com.kdt.coffeprince.repository.OrderItemRepository;
import com.kdt.coffeprince.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;
    private final Clock clock;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalTime CUTOFF = LocalTime.of(14, 0, 0);
    // A22: 주문 메모 수정 (취소된 주문도 가능, memo만 변경)
    @Transactional
    public OrderResponse changeMemo(Long orderId, String memo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        order.changeMemo(memo);
        return OrderResponse.from(order);
    }

    // ---------- A11 주문 접수 ----------
    // A16: 주문 취소
    @Transactional
    public OrderResponse place(OrderCreateRequest request) {
        String customerEmail = request.customerEmail().trim();
        String deliveryAddress = request.deliveryAddress().trim();
        String postcode = request.postcode().trim();

        List<Long> menuIds = request.items().stream()
                .map(OrderCreateRequest.Item::menuId).distinct().toList();

        Map<Long, Menu> menus = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getMenuId, Function.identity()));

        for (OrderCreateRequest.Item item : request.items()) {
            Menu menu = menus.get(item.menuId());
            if (menu == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴 못 찾음");
            }
            if (!menu.getIsAvailable()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "메뉴 안됨");
            }
        }

        Instant orderedAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        LocalDate deliveryDate = calculateDeliveryDate(orderedAt);

        boolean mismatch = orderRepository
                .findByCustomerEmailAndDeliveryDateAndStatus(customerEmail, deliveryDate, OrderStatus.PLACED)
                .stream()
                .anyMatch(o -> !o.getDeliveryAddress().trim().equals(deliveryAddress)
                        || !o.getPostcode().trim().equals(postcode));
        if (mismatch) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ADDRESS_MISMATCH");
        }

        // 주문 + 항목 저장 (한 트랜잭션, cascade)
        Order order = Order.place(customerEmail, deliveryAddress, postcode, orderedAt, deliveryDate, null);
        for (OrderCreateRequest.Item item : request.items()) {
            Menu menu = menus.get(item.menuId());
            order.addItem(new OrderItem(menu, item.quantity()));
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    // 서울 14:00:00 전이면 당일, 정각부터는 다음 날
    private LocalDate calculateDeliveryDate(Instant orderedAt) {
        ZonedDateTime seoul = orderedAt.atZone(SEOUL);
        return seoul.toLocalTime().isBefore(CUTOFF)
                ? seoul.toLocalDate()
                : seoul.toLocalDate().plusDays(1);
    }

    // ---------- A12 이메일별 주문 ----------
    public List<OrderSummaryResponse> findByCustomerEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
        return orderRepository.findByCustomerEmailOrderByOrderedAtDescOrderIdDesc(customerEmail.trim()).stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    // ---------- A13 주문 상세 ----------
    public OrderResponse find(Long orderId) {
    public OrderResponse cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
        return OrderResponse.from(order);
    }

    // ---------- A14 배송 묶음 ----------
    public List<DeliveryBatchResponse> findDeliveryBatches(LocalDate deliveryDate) {
        List<Order> orders = orderRepository.findByDeliveryDateAndStatus(deliveryDate, OrderStatus.PLACED);

        // 이메일 오름차순
        Map<String, List<Order>> byCustomerEmail = orders.stream()
                .collect(Collectors.groupingBy(Order::getCustomerEmail, TreeMap::new, Collectors.toList()));

        List<DeliveryBatchResponse> result = new ArrayList<>();
        byCustomerEmail.forEach((customerEmail, group) -> result.add(toBatch(deliveryDate, customerEmail, group)));
        return result;
    }

    private DeliveryBatchResponse toBatch(LocalDate deliveryDate, String customerEmail, List<Order> group) {
        List<Order> sorted = group.stream()
                .sorted(Comparator.comparing(Order::getOrderId))
                .toList();

        List<Long> orderIds = sorted.stream().map(Order::getOrderId).distinct().toList();
        Order first = sorted.get(0);   // 같은 묶음의 PLACED 주문은 주소가 같음

        // 메뉴 ID·단가·메뉴명이 모두 같을 때만 합산 (정렬: 메뉴 ID → 단가 → 메뉴명)
        Map<ItemKey, Integer> merged = new TreeMap<>(ITEM_KEY_ORDER);
        int totalQuantity = 0;
        long totalAmount = 0L;
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
                        e.getValue(), (long) e.getKey().unitPrice() * e.getValue()))
                .toList();

        return new DeliveryBatchResponse(deliveryDate, customerEmail,
                first.getDeliveryAddress(), first.getPostcode(),
                orderIds, orderIds.size(), totalQuantity, totalAmount, items);
    }

    private record ItemKey(Long menuId, int unitPrice, String menuName) {}

    private static final Comparator<ItemKey> ITEM_KEY_ORDER =
            Comparator.comparing(ItemKey::menuId, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparingInt(ItemKey::unitPrice)
                    .thenComparing(ItemKey::menuName);
}
