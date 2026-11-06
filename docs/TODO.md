# TODO

`requests/api-check.http`로 전체 API를 실서버에 실행해 보고 발견한 항목입니다. (2026-11-04 기준)
명세: 오류 응답은 `code·message·fieldErrors`를 포함하고, 400·404·409의 code·message는 오류 응답 표를 따른다. 내부 오류만 500.

파일 담당자가 다른 경우 직접 고치지 않고 여기에 기록만 해 두었습니다. 재현 요청은 `.http` 파일의 같은 번호 (A07 등) 항목을 실행하면 됩니다.

## 1. 500 이 나는 버그 (우선 수정)

- [ ] **A07 메뉴 상세** — 존재하지 않는 메뉴 ID·ID 0 으로 조회하면 500
    - 원인: `MenuService.one()`의 `orElseThrow()`가 예외 없이 호출돼 `NoSuchElementException` 발생, 처리기 없음
    - 기대: 404 `MENU_NOT_FOUND` / ID 0 은 400 `VALIDATION_ERROR`
- [x] **A13 주문 상세** — 존재하는 주문을 조회해도 500 (#47 에서 `@Transactional(readOnly = true)` 로 해결)
- [x] **A14 배송 묶음** — PLACED 주문이 있는 날짜로 조회하면 500 (#47 에서 A13 과 같이 해결)
- [ ] **A10 메뉴 삭제** — 주문에 사용된 메뉴를 삭제하면 500
    - 원인: `order_item.menu_id` FK 위반 (`DataIntegrityViolationException`). 삭제 전 주문 사용 여부 검사가 없음
    - 기대: 409 `MENU_IN_USE` (취소된 주문에 쓰인 메뉴도 포함)

## 2. 상태 코드는 맞지만 응답 형식이 명세와 다른 것

### MenuController (A07·A08·A09·A10·A20·A21)

- [ ] 경로 ID 타입 오류 (`/menus/abc`)·`@Positive` 위반 (`/menus/0/availability`)·쿼리 누락 (`/menus/search`)·JSON 자료형 오류
  (`"price":"abc"`)가 Spring 기본 형식 `{timestamp,status,error,path}` 로 나옴
    - `CategoryOrderExceptionHandler`가 `assignableTypes`로 Order·DeliveryBatches 컨트롤러에만 적용되어 있음.
      전역 `ValidationExceptionHandler` 로 합치기 (#51)
- [ ] `@Valid` 본문 검증 실패가 `{"code":"BAD_REQUEST","message":"공백일 수 없습니다"}` 로 나옴 (`ApiExceptionAdvice`의 `ApiError` 형식,
  `fieldErrors` 없음)
    - 기대: `{"code":"VALIDATION_ERROR","message":"입력값을 확인해 주세요.","fieldErrors":{"name":"..."}}`
- [ ] 404 가 `{"code":"NOT_FOUND","message":"메뉴를 찾을 수 없습니다."}` 로 나옴 (`NotFoundException` → `ApiError`)
    - 기대: code 는 `MENU_NOT_FOUND` / `CATEGORY_NOT_FOUND`, `fieldErrors: {}`. `NotFoundException` 대신
      `BusinessException(ErrorCode.MENU_NOT_FOUND)` 사용

### OrderService (A11·A12)

- [x] `OrderService.place()`·`findByCustomerEmail()`이 `ResponseStatusException`을 던져 본문이 Spring 기본 형식으로 나옴
  (#47 에서 `BusinessException` 으로 교체, A12 이메일 누락은 #52 에서 컨트롤러 `@CustomerEmail` 제약으로 처리)

### StatisticsController (A17·A18·A24)

- [ ] 미래 날짜 400 이 `{"code":"BAD_REQUEST","message":"미래 날짜는 조회할 수 없습니다"}` 로 나옴 (`org.apache.coyote.BadRequestException`
  사용)
    - 기대: `VALIDATION_ERROR` + `fieldErrors.date`
- [ ] 날짜 형식 오류 (`date=abc`)가 Spring 기본 형식으로 나옴

### DeliveryBatchesController (A14)

- [ ] 날짜 누락·형식 오류가 Spring 기본 형식으로 나옴. 기대: `VALIDATION_ERROR` + `fieldErrors.deliveryDate`

## 3. 요청·응답 이름이 명세와 다른 것

- [x] **A11 주문 접수 요청 필드**: 명세 `email`·`address` ↔ 구현 `customerEmail`·`deliveryAddress` (#47 에서 명세대로 교체)
- [x] **A12 쿼리**: 명세 `email` ↔ 구현 `customerEmail` (#47)
- [x] **A12 응답 필드**: `id·orderedAt·deliveryDate` 로 교체, `status` 는 `CANCELLED` 로 변환 (#47)
- [x] **A14 쿼리**: `shippingDate` 대신 `deliveryDate` 로 통일하기로 결정 (응답 필드도 동일)
- [ ] **A20 쿼리**: 명세 `keyword` ↔ 구현 `name`. 명세는 영문 대소문자 구분인데 구현은 `IgnoreCase`

## 4. 명세 입력 검사 중 빠진 것

- [ ] 메뉴: 가격 상한 1,000,000 / 설명 0~300자, `description` 필드 필수 (누락 시 400) / 이름·설명 앞뒤 공백 제거
- [ ] 메뉴명 검색: 검색어 앞뒤 공백 제거 후 1~100자 (현재 `name=` 빈 문자열이면 200 + 전체 메뉴)
- [x] 주문: 항목 1~10개 / `menuId` 중복 금지 / 수량 1~20 / 주소 1 ~200자 / 우편번호 숫자 5자리 / 이메일 1~100자 + 명세 정규식 (#52)
- [x] 주문 ID: `GET /orders/{orderId}` 에 `@Positive` 없음 → ID 0 이 400 이 아니라 404 (#52)
- [ ] 날짜 쿼리: 2000-01-01~2099-12-31 범위 검사 (A14·A17·A18·A24)
- [ ] 카테고리 이름: 명세는 앞뒤 공백 제거 후 1~30자인데 `CategoryRequest`는 `@Size(max = 50)` (엔티티 길이도 50). 30 으로 맞추고 trim 후 검사

## 5. 보류 (코드 일관성 정리 후 진행)

- [ ] **문자열 입력 trim 을 애노테이션으로 통일** — `CategoryRequest`·`CategoryUpdateRequest`·`CreateMenuRequest`·`PutMenuRequest`의
  컴팩트 생성자와 `CategoryService`·`OrderService`에 같은 `trim()` 로직이 흩어져 있음
    - 방안: `@Trimmed` 커스텀 애노테이션 (`@JacksonAnnotationsInside` + `@JsonDeserialize(using = TrimmedStringDeserializer.class)`)
      을 만들어 DTO 필드에 붙이고 생성자·서비스의 trim 제거. JSON 본문에만 적용되므로 쿼리 파라미터(`MenuSearchRequest`)는 별도
    - 검증 흐름은 그대로: trim 후 `@NotBlank`·`@Size` → `MethodArgumentNotValidException` → `ValidationExceptionHandler`
    - `{"name": 1}` 같은 자료형 오류는 현재 Jackson 이 `"1"` 로 변환해 통과시킴. 막으려면 역직렬화기에서 `VALUE_STRING` 토큰 검사
      추가 (전역은 `coercionConfigFor(LogicalType.Textual)` 설정). 널널하게 보기로 하여 당장은 적용 안 함
    - `OrderService` 는 #52 에서 Reorder 와 통합됐고 요청 DTO 의 trim 은 `OrderCreateRequest` 컴팩트 생성자로 옮김.
      재주문·배송지 비교의 `trim()` 은 엔티티 값 대상이라 그대로 둠
