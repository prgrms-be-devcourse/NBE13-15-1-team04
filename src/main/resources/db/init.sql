SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS category (
    category_id BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(30)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS menu (
    menu_id      BIGINT       NOT NULL AUTO_INCREMENT,
    category_id  BIGINT       NOT NULL,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(300) NULL,
    price        INT          NOT NULL,
    is_available BIT(1)       NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (menu_id),
    CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS orders (
    order_id          BIGINT       NOT NULL AUTO_INCREMENT,
    reorder_source_id BIGINT       NULL,
    customer_email    VARCHAR(100) NOT NULL,
    customer_phone    VARCHAR(20)  NULL,
    delivery_address  VARCHAR(200) NOT NULL,
    postcode          VARCHAR(5)   NOT NULL,
    delivery_date     DATE         NOT NULL,
    memo              VARCHAR(255) NULL,
    status            VARCHAR(20)  NOT NULL,
    total_amount      INT          NOT NULL,
    ordered_at        DATETIME(6)  NOT NULL,
    canceled_at       DATETIME(6)  NULL,
    updated_at        DATETIME(6)  NOT NULL,
    PRIMARY KEY (order_id),
    INDEX idx_orders_customer_email (customer_email),
    INDEX idx_orders_delivery_date  (delivery_date, status),
    INDEX idx_orders_ordered_at     (ordered_at),
    CONSTRAINT fk_orders_reorder_source FOREIGN KEY (reorder_source_id) REFERENCES orders (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS order_item (
    order_item_id BIGINT       NOT NULL AUTO_INCREMENT,
    order_id      BIGINT       NOT NULL,
    menu_id       BIGINT       NOT NULL,
    menu_name     VARCHAR(100) NOT NULL,
    unit_price    INT          NOT NULL,
    quantity      INT          NOT NULL,
    line_amount   BIGINT       NOT NULL,
    PRIMARY KEY (order_item_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_order_item_menu  FOREIGN KEY (menu_id)  REFERENCES menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO category (category_id, name, created_at, updated_at) VALUES
    (1, 'Single Origin', NOW(6), NOW(6)),
    (2, 'Blend', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    updated_at = NOW(6);

INSERT INTO menu (menu_id, category_id, name, description, price, is_available, created_at, updated_at) VALUES
    (1, 1, 'Columbia Nariñó', '콜롬비아 나리뇨 원두를 담은 커피빈 패키지입니다.', 5000, 1, NOW(6), NOW(6)),
    (2, 1, 'Brazil Serra Do Caparaó', '브라질 세하 두 카파라오 원두를 담은 커피빈 패키지입니다.', 7000, 1, NOW(6), NOW(6)),
    (3, 1, 'Columbia Quindío (White Wine Extended Fermentation)', '콜롬비아 킨디오 원두를 담은 커피빈 패키지입니다.', 9000, 1, NOW(6), NOW(6)),
    (4, 1, 'Ethiopia Sidamo', '에티오피아 시다모 원두를 담은 커피빈 패키지입니다.', 11000, 1, NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    category_id = VALUES(category_id),
    name = VALUES(name),
    description = VALUES(description),
    price = VALUES(price),
    is_available = VALUES(is_available),
    updated_at = NOW(6);

-- ---------------------------------------------------------------------------
-- 주문·주문 상세 초기 데이터 (#56)
--  - 날짜는 적재 시점 기준 상대값으로 계산해 재실행해도 "미래 배송일" 주문이 유지되도록 함
--    · delivery_date: 서울 기준 날짜(CURDATE(), docker-compose 의 MySQL TZ=Asia/Seoul)
--    · ordered_at·canceled_at·updated_at: JPA 가 Instant 를 UTC 로 저장하므로 UTC_TIMESTAMP(6) 사용
--  - 같은 이메일·배송일의 PLACED 주문은 주소·우편번호가 같아야 함 (OrderService.validateSameAddress)
--  - 1: buyer / 지난 배송 (PLACED)
--  - 2: buyer / 내일 배송 (PLACED, 취소 가능)
--  - 3: buyer / 취소된 주문 (CANCELED)
--  - 4: buyer / 1번 주문의 재주문 (PLACED, 내일 배송, 2번과 같은 주소)
--  - 5: other / 내일 배송 (PLACED, 전화번호·메모 포함) → A14 배송 묶음에서 2·4번과 같은 날
--  - 6: other / 오늘 배송 (PLACED)
-- ---------------------------------------------------------------------------
INSERT INTO orders (order_id, reorder_source_id, customer_email, customer_phone, delivery_address, postcode,
                    delivery_date, memo, status, total_amount, ordered_at, canceled_at, updated_at) VALUES
    (1, NULL, 'buyer@example.com', NULL,            '서울특별시 강남구 테헤란로 123 4층', '06234',
        DATE_SUB(CURDATE(), INTERVAL 3 DAY), '',                   'PLACED',   17000,
        DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 3 DAY), NULL,                              DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 3 DAY)),
    (2, NULL, 'buyer@example.com', NULL,            '서울특별시 강남구 테헤란로 123 4층', '06234',
        DATE_ADD(CURDATE(), INTERVAL 1 DAY), '',                   'PLACED',   20000,
        DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 2 HOUR), NULL,                             DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 2 HOUR)),
    (3, NULL, 'buyer@example.com', NULL,            '서울특별시 강남구 테헤란로 123 4층', '06234',
        DATE_SUB(CURDATE(), INTERVAL 1 DAY), '',                   'CANCELED', 5000,
        DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 2 DAY), DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 47 HOUR), DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 47 HOUR)),
    (4, 1,    'buyer@example.com', NULL,            '서울특별시 강남구 테헤란로 123 4층', '06234',
        DATE_ADD(CURDATE(), INTERVAL 1 DAY), '',                   'PLACED',   17000,
        DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 1 HOUR), NULL,                             DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 1 HOUR)),
    (5, NULL, 'other@example.com', '010-1234-5678', '경기도 성남시 분당구 판교역로 235',   '13494',
        DATE_ADD(CURDATE(), INTERVAL 1 DAY), '부재 시 경비실에 맡겨 주세요', 'PLACED', 21000,
        DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 30 MINUTE), NULL,                          DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 30 MINUTE)),
    (6, NULL, 'other@example.com', '010-1234-5678', '경기도 성남시 분당구 판교역로 235',   '13494',
        CURDATE(),                           '',                   'PLACED',   16000,
        DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 1 DAY), NULL,                              DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE
    reorder_source_id = VALUES(reorder_source_id),
    customer_email    = VALUES(customer_email),
    customer_phone    = VALUES(customer_phone),
    delivery_address  = VALUES(delivery_address),
    postcode          = VALUES(postcode),
    delivery_date     = VALUES(delivery_date),
    memo              = VALUES(memo),
    status            = VALUES(status),
    total_amount      = VALUES(total_amount),
    ordered_at        = VALUES(ordered_at),
    canceled_at       = VALUES(canceled_at),
    updated_at        = VALUES(updated_at);

-- menu_name·unit_price 는 주문 시점 스냅샷 (menu 초기 데이터와 동일한 값)
INSERT INTO order_item (order_item_id, order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (1, 1, 1, 'Columbia Nariñó',                                      5000, 2, 10000),
    (2, 1, 2, 'Brazil Serra Do Caparaó',                              7000, 1,  7000),
    (3, 2, 3, 'Columbia Quindío (White Wine Extended Fermentation)',  9000, 1,  9000),
    (4, 2, 4, 'Ethiopia Sidamo',                                     11000, 1, 11000),
    (5, 3, 1, 'Columbia Nariñó',                                      5000, 1,  5000),
    (6, 4, 1, 'Columbia Nariñó',                                      5000, 2, 10000),
    (7, 4, 2, 'Brazil Serra Do Caparaó',                              7000, 1,  7000),
    (8, 5, 2, 'Brazil Serra Do Caparaó',                              7000, 3, 21000),
    (9, 6, 1, 'Columbia Nariñó',                                      5000, 1,  5000),
    (10, 6, 4, 'Ethiopia Sidamo',                                    11000, 1, 11000)
ON DUPLICATE KEY UPDATE
    order_id    = VALUES(order_id),
    menu_id     = VALUES(menu_id),
    menu_name   = VALUES(menu_name),
    unit_price  = VALUES(unit_price),
    quantity    = VALUES(quantity),
    line_amount = VALUES(line_amount);
