# ERD

`TODO.md`의 항목까지 반영된 **최종 구조** 기준입니다. 현재 코드와 다른 부분은 `TODO.md`를 참고하세요.

```mermaid
erDiagram
    category ||--o{ menu : "has"
    orders ||--|{ order_item : "contains"
    menu |o--o{ order_item : "snapshot of"
    orders |o--o{ orders : "reorder_source"

    category {
        BIGINT category_id PK "AUTO_INCREMENT"
        VARCHAR(50) name "NOT NULL"
        VARCHAR(255) description
        DATETIME created_at "NOT NULL"
        DATETIME updated_at "NOT NULL"
    }

    menu {
        BIGINT menu_id PK "AUTO_INCREMENT"
        BIGINT category_id FK "NOT NULL"
        VARCHAR(100) name "NOT NULL"
        VARCHAR(255) description
        INT price "NOT NULL, 원"
        BOOLEAN is_available "NOT NULL, DEFAULT 1 (A21)"
        DATETIME created_at "NOT NULL"
        DATETIME updated_at "NOT NULL"
    }

    orders {
        BIGINT order_id PK "AUTO_INCREMENT"
        BIGINT reorder_source_id FK "NULL, 재주문 원본 (A23)"
        VARCHAR(100) customer_email "NOT NULL (A12, A24)"
        VARCHAR(50) customer_name "NOT NULL"
        VARCHAR(20) customer_phone
        VARCHAR(255) delivery_address "NOT NULL"
        DATE delivery_date "NOT NULL"
        VARCHAR(255) memo "(A22)"
        ENUM status "NOT NULL, PLACED / CANCELED, DEFAULT PLACED"
        INT total_amount "NOT NULL, DEFAULT 0"
        DATETIME ordered_at "NOT NULL, 접수 일시 (통계 기준)"
        DATETIME canceled_at "NULL, 취소 일시 (A16)"
        DATETIME updated_at "NOT NULL"
    }

    order_item {
        BIGINT order_item_id PK "AUTO_INCREMENT"
        BIGINT order_id FK "NOT NULL"
        BIGINT menu_id FK "NULL, 메뉴 삭제 시 NULL"
        VARCHAR(100) menu_name "NOT NULL, 주문 시점 스냅샷"
        INT unit_price "NOT NULL, 주문 시점 스냅샷"
        INT quantity "NOT NULL, 1 이상"
    }
```

## 관계 / FK 정책

| FK | 참조 | ON DELETE | 이유 |
|---|---|---|---|
| `menu.category_id` | `category.category_id` | RESTRICT (기본) | 메뉴가 남아 있는 카테고리는 삭제 불가 |
| `order_item.order_id` | `orders.order_id` | CASCADE | 주문 항목은 주문에 종속. JPA에서도 `cascade = ALL, orphanRemoval = true` |
| `order_item.menu_id` | `menu.menu_id` | SET NULL | 메뉴가 삭제돼도 주문 내역은 스냅샷(`menu_name`, `unit_price`)으로 유지 |
| `orders.reorder_source_id` | `orders.order_id` | SET NULL | 원본 주문이 삭제돼도 재주문은 독립적으로 유지 |

## 엔티티 매핑 메모

| 항목 | 내용 |
|---|---|
| PK 생성 | 모든 엔티티 `IDENTITY` 전략. DB에 `AUTO_INCREMENT` 필요 |
| PK 필드명 | `xxxId` 규칙 (`categoryId`, `menuId`, `orderId`, `orderItemId`) |
| 시각 타입 | `DATETIME` → `Instant`, `DATE` → `LocalDate` |
| `created_at` / `ordered_at` | JPA Auditing `@CreatedDate`, `updatable = false` |
| `updated_at` | JPA Auditing `@LastModifiedDate`. DB에 `ON UPDATE CURRENT_TIMESTAMP`가 없으므로 애플리케이션이 채움 |
| `canceled_at` | Auditing이 아닌 취소 로직에서 `status`와 함께 설정 (도메인 메서드는 추후 작성, `TODO.md`) |
| `orders.status` | `@Enumerated(EnumType.STRING)`, `constant/OrderStatus` (`PLACED`, `CANCELED`) |
| `orders.total_amount` | `order_item.unit_price × quantity` 합계. 항목 추가 시 함께 갱신 (도메인 메서드는 추후 작성, `TODO.md`) |
| `order_item.menu_name` / `unit_price` | `OrderItem` 생성 시 `Menu`에서 복사. 이후 메뉴 변경과 무관 |
| 연관관계 주인 | `menu.category` → `Category.menus`(mappedBy), `order_item.order` → `Order.items`(mappedBy). `order_item.menu`, `orders.reorder_source`는 단방향 |
| fetch | 모든 `@ManyToOne`은 `LAZY`. 응답은 엔티티 대신 DTO(`record` + `from()`)로 변환 |
