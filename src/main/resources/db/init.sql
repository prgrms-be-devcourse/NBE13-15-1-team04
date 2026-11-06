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
