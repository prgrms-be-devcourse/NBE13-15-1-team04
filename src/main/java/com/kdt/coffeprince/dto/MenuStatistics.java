package com.kdt.coffeprince.dto;

import com.kdt.coffeprince.entity.Order;
import com.kdt.coffeprince.entity.OrderItem;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// A18 접수일별 메뉴 통계 한 줄. PLACED 주문만 집계한다.
// 명세: 메뉴 ID·주문 당시 메뉴명·단가가 모두 같은 항목끼리 합산하고 메뉴 ID → 단가 → 메뉴명 오름차순
// (같은 날 가격이 바뀐 메뉴는 단가별로 따로 한 줄씩 나온다)
public record MenuStatistics(
        long menuId,
        String menuName,
        int unitPrice,
        int totalQuantity,
        int totalAmount
) {
    // 합산 키: 메뉴 ID + 단가 + 주문 당시 메뉴명 (A14 배송 묶음의 항목 합산과 같은 규칙)
    private record ItemKey(Long menuId, int unitPrice, String menuName) {
    }

    private static final Comparator<ItemKey> ITEM_KEY_ORDER =
            Comparator.comparing(ItemKey::menuId, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparingInt(ItemKey::unitPrice)
                    .thenComparing(ItemKey::menuName);

    public static List<MenuStatistics> listFrom(List<Order> orders) {
        Map<ItemKey, Integer> quantities = new TreeMap<>(ITEM_KEY_ORDER);
        for (Order order : Order.filterPlaced(orders)) {
            for (OrderItem item : order.getItems()) {
                quantities.merge(new ItemKey(item.getMenuId(), item.getUnitPrice(), item.getMenuName()),
                        item.getQuantity(), Integer::sum);
            }
        }
        return quantities.entrySet().stream()
                .map(e -> new MenuStatistics(
                        e.getKey().menuId(), e.getKey().menuName(), e.getKey().unitPrice(),
                        e.getValue(), e.getKey().unitPrice() * e.getValue()))
                .toList();
    }
}
