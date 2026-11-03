-- local 프로파일 초기 데이터. 매 기동마다 실행되므로 PK 를 고정하고 ON DUPLICATE KEY UPDATE 로 중복을 막는다.
-- 컬럼명은 엔티티에서 Hibernate 가 생성하는 스키마 기준 (docs/TODO.md 참고).

INSERT INTO category (category_id, name, description, created_at, updated_at) VALUES
    (1, 'Single Origin', 'Coffee beans from a single farm or region.', NOW(6), NOW(6)),
    (2, 'Blend', 'Coffee beans blended from multiple origins.', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
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