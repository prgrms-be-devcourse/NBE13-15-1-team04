# TODO

주문 도메인(`Order`, `OrderItem`) 작업 중 발견했지만, 충돌 방지를 위해 다른 작업자의 파일은 건드리지 않고 기록만 해 둔 항목입니다.
각 항목은 `ERD.md`의 최종 구조를 기준으로 합니다.

## 공통 (현재는 엔티티 뼈대만 작성된 상태)

- [x] 모든 엔티티에 `@NoArgsConstructor(access = PROTECTED)` 추가
  - `@Builder`는 사용하지 않기로 함. 객체 생성용 생성자는 도메인 메서드 작업 시 함께 작성
- [ ] `Order` 도메인 메서드
  - 항목 추가: 양방향 연관관계 세팅 + `totalAmount` 갱신을 한 곳에서 처리
  - 취소: `status = CANCELED`와 `canceledAt` 설정을 함께 처리, 중복 취소 방지
  - `getItems()`는 불변 리스트로 반환해 `totalAmount`와 어긋나는 외부 수정 방지
- [ ] `OrderItem` 생성 시 `Menu`에서 `menuName`, `unitPrice` 스냅샷 복사, `quantity < 1` 검증

## Category / Menu 엔티티

- [x] `Category`, `Menu`에 `@EntityListeners(AuditingEntityListener.class)` 추가
  - `@CreatedDate` / `@LastModifiedDate`는 리스너가 있어야 값이 채워짐. 없으면 `created_at NOT NULL` 때문에 INSERT 실패
  - `@EnableJpaAuditing`은 `config/JpaConfig.java`에 이미 있음
- [x] `Category.id` → `categoryId` + `@Column(name = "category_id")` (PK는 `xxxId` 규칙으로 통일)
- [x] `Category.name`에 `nullable = false` (DDL `NOT NULL`)
- [x] `Category.createdAt`에 `updatable = false`, `Category.updatedAt`에 `nullable = false` (DDL 기준)
- [x] `Category.menus`의 `cascade = ALL, orphanRemoval = true` 재검토
  - 카테고리를 삭제하면 메뉴가 같이 삭제됨. 의도한 동작인지 확인 필요
  - → `ERD.md`(RESTRICT)와 `CategoryService.delete`(메뉴 있으면 409)에 맞춰 cascade/orphanRemoval 제거
- [x] `Menu.id` → `menuId` + `@Column(name = "menu_id")` (PK는 `xxxId` 규칙으로 통일)
- [x] 위 두 PK 컬럼명을 바꿀 때 `src/main/resources/db/data-local.sql`의 `id` → `category_id` / `menu_id`도 같이 수정
  - 초기 데이터 SQL은 Hibernate가 엔티티에서 만드는 컬럼명을 그대로 쓰고 있음

## DTO / 서비스

- [x] `MenuResponse.from()`의 `menu.getId()` → `menu.getMenuId()`, `category.getId()` → `category.getCategoryId()`
  - 위 PK 네이밍 통일과 함께 수정해야 컴파일됨
  - `CategoryResponse.from()`, `CategoryRepository`의 derived query(`...ByCategoryId...`)·JPQL도 함께 수정
- [x] 주문 목록 조회 시 `Order.items` N+1 대비
  - fetch join 또는 `spring.jpa.properties.hibernate.default_batch_fetch_size` 설정
  - → `application.yaml`에 `default_batch_fetch_size: 100` 추가. 목록 API 작성 시 fetch join 필요 여부는 별도 판단
- [ ] 재주문 구현 시 원본 `OrderItem`을 재사용하지 말고 새로 생성
  - `Order.items`에 `orphanRemoval = true`라 원본 주문에서 항목이 빠져 버림

## DB

- [ ] 모든 PK에 `AUTO_INCREMENT` 추가 (엔티티는 `IDENTITY` 전략)
- [ ] `orders.status`를 코드(`OrderStatus`)에 맞춰 `ENUM('PLACED','CANCELED') DEFAULT 'PLACED'`로 변경
  - 원본 DDL은 `ENUM NOT NULL DEFAULT 'RECEIVED'`로 값 목록이 없고 기본값도 코드와 다름
- [ ] FK 제약 추가 (원본 DDL에 없음). 정책은 `ERD.md` 참고

```sql
ALTER TABLE category   MODIFY category_id   BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE menu       MODIFY menu_id       BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE orders     MODIFY order_id      BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE order_item MODIFY order_item_id BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE orders MODIFY status ENUM('PLACED','CANCELED') NOT NULL DEFAULT 'PLACED';

ALTER TABLE menu       ADD CONSTRAINT fk_menu_category      FOREIGN KEY (category_id)       REFERENCES category(category_id);
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_order   FOREIGN KEY (order_id)          REFERENCES orders(order_id) ON DELETE CASCADE;
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_menu    FOREIGN KEY (menu_id)           REFERENCES menu(menu_id)    ON DELETE SET NULL;
ALTER TABLE orders     ADD CONSTRAINT fk_orders_reorder_src FOREIGN KEY (reorder_source_id) REFERENCES orders(order_id) ON DELETE SET NULL;
```
