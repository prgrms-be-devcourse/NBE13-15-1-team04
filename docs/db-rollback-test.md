# 실제 DB 저장 및 롤백 검증

## 1. 테스트 환경

- 프로젝트: CoffePrince
- 데이터베이스: MySQL 8.4 (Docker)
- DB 이름: `coffeprince`
- 대상 테이블: `menu`
- 검증 목적: 실제 DB 저장과 롤백 동작 확인

## 2. MySQL 수동 롤백 테스트

### 실행 SQL

```sql
START TRANSACTION;

INSERT INTO menu (
    category_id,
    name,
    description,
    price,
    is_available,
    created_at,
    updated_at
)
VALUES (
    1,
    'ROLLBACK_TEST_MENU',
    ' ',
    1000,
    TRUE,
    NOW(),
    NOW()
);

SET @test_menu_id = LAST_INSERT_ID();

SELECT menu_id, category_id, name, price
FROM menu
WHERE menu_id = @test_menu_id;

ROLLBACK;

SELECT menu_id, category_id, name, price
FROM menu
WHERE menu_id = @test_menu_id;
```

### 실제 실행 결과

- INSERT 성공: `1 row affected`
- INSERT 이후 조회: 메뉴 ID `6` 확인
- ROLLBACK 실행 성공
- ROLLBACK 이후 조회: `Empty set`

**판정: 성공**

## 3. Spring 자동 롤백 테스트

### 테스트 코드

`src/test/java/com/kdt/coffeprince/TransactionRollbackVerificationTest.java`

### 검증 절차

1. Spring Boot 테스트 환경에서 실제 MySQL 연결
2. `TransactionTemplate`을 사용하여 트랜잭션 시작
3. `menuRepository.saveAndFlush()`로 테스트 메뉴 INSERT 실행
4. 트랜잭션 내부에서 메뉴 존재 확인
5. `IllegalStateException`을 의도적으로 발생시킴
6. Spring 트랜잭션 관리자가 롤백 처리
7. 트랜잭션 종료 후 메뉴가 존재하지 않음을 검증

### 실제 실행 결과

```text
자동 롤백 검증 성공! menu_id=7
BUILD SUCCESSFUL in 8s
```

**판정: 성공**

## 4. 검증 범위 및 결론

MySQL 수동 롤백과 Spring 트랜잭션 관리자의 예외 발생 시 자동 롤백이 정상 동작함을 확인했습니다.

단, 자동 롤백 검증은 `TransactionTemplate`을 사용한 독립적인 검증이며, 개별 서비스 메서드의 `@Transactional` 적용 여부까지 직접 검증한 것은 아닙니다.

또한 테스트에 사용된 메뉴 데이터는 롤백되어 DB에 남지 않습니다.