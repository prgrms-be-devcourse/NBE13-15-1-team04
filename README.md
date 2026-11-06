# coffe-prince

## 🏗 시스템 아키텍처

![시스템 아키텍처](./docs/images/coffe-prince-architecture.svg)

## 🗃 데이터베이스 ERD

![ERD](./docs/images/coffe-prince-erd.svg)

본 프로젝트는 카테고리, 메뉴, 주문, 주문 항목의
4개 테이블을 중심으로 구성되어 있습니다.

자세한 설계 내용은 [ERD 문서](./docs/ERD.md)를 참고해 주세요.

## 📡 API 명세

Coffe Prince는 카테고리, 메뉴, 주문, 배송 및 통계 기능을 REST API로 제공합니다.

**기본 API 주소:** `http://localhost:8080/api/v1`

**Swagger UI:** `http://localhost:8080/swagger-ui/index.html` (로컬 서버 실행 후 접속)

### 주요 API 목록

| 구분 | Method | Endpoint | 기능 |
|---|---|---|---|
| 카테고리 | GET | `/categories` | 카테고리 목록 조회 |
| 카테고리 | POST | `/categories` | 카테고리 등록 |
| 카테고리 | GET | `/categories/{categoryId}/menus` | 카테고리별 메뉴 조회 |
| 메뉴 | GET | `/menus` | 전체 메뉴 조회 |
| 메뉴 | GET | `/menus/{menuId}` | 메뉴 상세 조회 |
| 메뉴 | POST | `/menus` | 메뉴 등록 |
| 메뉴 | PUT | `/menus/{menuId}` | 메뉴 수정 |
| 메뉴 | DELETE | `/menus/{menuId}` | 메뉴 삭제 |
| 메뉴 | GET | `/menus/available` | 판매 가능 메뉴 조회 |
| 메뉴 | GET | `/menus/search?keyword={keyword}` | 메뉴명 검색 |
| 메뉴 | PUT | `/menus/{menuId}/availability` | 메뉴 판매 여부 변경 |
| 주문 | POST | `/orders` | 주문 접수 |
| 주문 | GET | `/orders?email={email}` | 이메일별 주문 조회 |
| 주문 | GET | `/orders/{orderId}` | 주문 상세 조회 |
| 주문 | POST | `/orders/{orderId}/cancel` | 주문 취소 |
| 주문 | POST | `/orders/{orderId}/reorder` | 재주문 |
| 주문 | PUT | `/orders/{orderId}/memo` | 주문 메모 수정 |
| 배송 | GET | `/delivery-batches?deliveryDate={date}` | 배송일별 주문 묶음 조회 |
| 통계 | GET | `/statistics/orders?date={date}` | 주문 통계 |
| 통계 | GET | `/statistics/menus?date={date}` | 메뉴 통계 |
| 통계 | GET | `/statistics/customers?date={date}` | 고객 통계 |

카테고리 상세 조회·수정·삭제 등 나머지 API와 구체적인 요청·응답 형식은 Swagger UI에서 확인할 수 있습니다.

추가 심화 기능으로 RestClient를 활용한 외부 게시글 조회 API(`GET /reference/posts/{postId}`)도 구현되어 있습니다.

### 대표 API 요청 예시

**메뉴 등록 — POST /api/v1/menus**

```http
POST /api/v1/menus
Content-Type: application/json

{
  "categoryId": 1,
  "name": "House Blend",
  "price": 6000,
  "description": "블렌드 원두 패키지입니다."
}
```

메뉴 등록에 성공하면 `201 Created` 상태 코드와 등록된 메뉴 정보를 반환합니다.

### API 문서 및 테스트

- **Swagger UI:** 로컬 서버 실행 후 `http://localhost:8080/swagger-ui/index.html`
- **HTTP 요청 테스트:** [api-check.http](./requests/api-check.http)

※ API 목록은 현재 `dev` 브랜치의 Controller 코드를 기준으로 작성했습니다. 최종 제출 전 실제 API 테스트 결과를 확인해야 합니다.

## ⚙️ 핵심 구현 방식 및 설계 포인트

### 1. Redis 기반 메뉴 목록 캐싱

전체 메뉴 조회(`GET /api/v1/menus`)는 동일한 결과가 반복해서 요청될 수 있어 Spring Cache와 Redis를 적용했습니다.

- `@Cacheable`로 메뉴 목록 조회 결과를 `menus::all` 키에 저장하고, TTL을 60초로 설정했습니다.
- 메뉴 등록·수정·삭제·판매 여부 변경 시 `@CacheEvict`로 관련 캐시를 삭제하도록 구성했습니다.
- 캐시를 비활성화하면 DB를 직접 조회할 수 있도록 `CACHE_ENABLED` 설정을 제공합니다.

**설계 의도:** 반복 조회 시 불필요한 DB 접근을 줄이면서 메뉴 변경 사항이 이전 캐시에 계속 남지 않도록 처리합니다. 실제 응답 속도 개선 수치는 별도의 성능 측정이 필요합니다.

관련 코드: [RedisCacheConfig.java](./src/main/java/com/kdt/coffeprince/config/RedisCacheConfig.java), [MenuService.java](./src/main/java/com/kdt/coffeprince/service/MenuService.java)

### 2. 주문과 주문 항목의 트랜잭션 처리

주문 생성은 주문 정보와 여러 주문 항목을 함께 저장해야 하는 작업입니다.

- `OrderService.place()`에 `@Transactional`을 적용했습니다.
- `Order`와 `OrderItem`은 JPA 연관관계 및 `CascadeType.ALL`을 통해 함께 저장합니다.
- 주문 접수와 재주문은 공통 `createOrder()` 로직을 사용하며, 메뉴 판매 가능 여부와 배송지 조건을 확인한 뒤 저장합니다.

**설계 의도:** 주문 처리 중 오류가 발생했을 때 주문 정보와 주문 항목이 일부만 저장되는 상황을 방지합니다.

관련 코드: [OrderService.java](./src/main/java/com/kdt/coffeprince/service/OrderService.java), [Order.java](./src/main/java/com/kdt/coffeprince/entity/Order.java)

### 3. 주문 시점의 메뉴 정보 보존

주문 이후 메뉴 이름이나 가격이 변경되더라도 기존 주문 금액과 상품 내역은 주문 당시 정보로 확인할 수 있도록 구성했습니다.

- `OrderItem` 생성 시 `Menu`의 이름과 가격을 `menuName`, `unitPrice`에 별도 저장합니다.
- 수량과 주문 당시 단가로 `lineAmount`를 계산합니다.
- 주문에서 사용 중인 메뉴는 삭제 전에 사용 여부를 확인하고 `MENU_IN_USE` 예외로 차단합니다.

**설계 의도:** 메뉴 정보의 변경이 과거 주문 내역을 바꾸지 않도록 하며, 참조 중인 메뉴를 임의로 삭제하지 못하게 합니다.

관련 코드: [OrderItem.java](./src/main/java/com/kdt/coffeprince/entity/OrderItem.java), [MenuService.java](./src/main/java/com/kdt/coffeprince/service/MenuService.java)

### 4. 업무 규칙에 따른 배송일·취소 기한 계산

주문 접수와 취소에는 서울 시간 기준의 업무 규칙을 적용했습니다.

- 주문 접수 시각이 **14:00 이전**이면 배송일을 당일로, **14:00부터**는 다음 날로 계산합니다.
- 주문 취소는 **저장된 배송일의 14:00 이전**에만 가능하도록 검사합니다.
- 취소 시 주문 상태와 취소 시각을 변경하되 기존 주문 항목과 금액은 유지합니다.

**설계 의도:** 주문 처리 기준 시간을 명확히 하고, 취소 이후에도 기존 주문 이력을 확인할 수 있도록 합니다.

관련 코드: [OrderService.java](./src/main/java/com/kdt/coffeprince/service/OrderService.java), [Order.java](./src/main/java/com/kdt/coffeprince/entity/Order.java)

> 위 내용은 현재 `dev` 브랜치의 구현 코드를 기준으로 정리했습니다. 성능 개선 수치나 운영 환경에서의 검증 결과를 의미하지는 않습니다.

## 실행 방법 (로컬)

1. 로컬 MySQL을 띄우고 `coffeprince` 데이터베이스를 만듭니다.
   ```sql
   CREATE DATABASE coffeprince CHARACTER SET utf8mb4;
   ```
   기본 접속 정보는 `localhost:3306` / `root` / 비밀번호 없음입니다.
2. 접속 정보가 다르면 루트에 `local.env` 파일을 만듭니다 (`*.env`는 gitignore 대상).
   ```properties
   DB_URL=jdbc:mysql://localhost:3306/coffeprince
   DB_USERNAME=root
   DB_PASSWORD=
   ```
3. 애플리케이션을 실행합니다.
   ```bash
   ./gradlew bootRun        # 실행 권한이 없으면 sh ./gradlew bootRun
   ```
   프로파일을 지정하지 않으면 `local`로 실행되며, 테이블 생성과 초기 데이터 적재가 자동으로 이루어집니다.
   DB가 아직 준비되지 않았으면 최대 30초 동안 재시도한 뒤 실패합니다.

## 프로파일

| 프로파일 | `ddl-auto` | 초기 데이터 | 접속 정보 |
|---|---|---|---|
| `local` (기본) | `update` — 엔티티 기준으로 테이블 생성·변경 | 매 기동마다 적재 | `local.env` 또는 위 기본값 |
| `prod` | `none` — 스키마를 건드리지 않음 | 적재하지 않음 | `prod.env` 또는 환경 변수 **필수** (없으면 기동 실패) |

프로파일마다 자기 이름의 env 파일(`local.env`, `prod.env`)만 읽습니다. 전환은 환경 변수로 합니다.
```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

설정 파일: `src/main/resources/application.yaml`(공통), `application-local.yaml`, `application-prod.yaml`

## 초기 데이터

- 파일: `src/main/resources/db/data-local.sql`
- `local` 프로파일에서 **매 기동마다** 실행됩니다. 모든 문장이 PK를 고정하고 `ON DUPLICATE KEY UPDATE`를 쓰므로 여러 번 실행해도 중복되지 않습니다.
- 데이터를 바꾸려면 이 파일을 수정하고 재기동하면 됩니다. 이미 있는 행은 파일 내용으로 덮어써집니다.

적재되는 데이터

| 테이블 | 내용 |
|---|---|
| `category` | 1 Single Origin, 2 Blend |
| `menu` | Columbia Nariñó 5,000 / Brazil Serra Do Caparaó 7,000 / Columbia Quindío (White Wine Extended Fermentation) 9,000 / Ethiopia Sidamo 11,000 — 모두 Single Origin, 판매 중 |

## 주의

- `data-local.sql`의 컬럼명은 **엔티티 기준**입니다. 엔티티 필드나 컬럼명을 바꾸면 이 파일도 같이 고쳐야 합니다 (`docs/TODO.md` 참고).
- 운영 DB를 가리키는 상태에서 `local` 프로파일로 실행하지 마세요. `ddl-auto: update`가 운영 스키마를 바꿉니다.

## Redis 메뉴 조회 캐시

| 항목 | 내용 |
|---|---|
| 대상 | **A06** `GET /api/v1/menus` |
| 키 / TTL | `menus::all` / **60초** (변경 시 즉시 삭제하므로 TTL은 안전장치) |
| 캐시 삭제 | 메뉴 등록·수정·삭제·판매 여부 변경(A08·A09·A10·A21) 시 |
| 코드 | `config/RedisCacheConfig.java`, `MenuService`의 `@Cacheable`·`@CacheEvict` |

**실행** (캐시는 기본 켜짐 — 앱 실행 전에 Redis를 띄워야 합니다)
```bash
docker compose up -d redis          # Redis 실행
# Redis 없이 실행하려면 local.env 에 CACHE_ENABLED=false 추가 후 앱 재시작
```

**확인 방법**
```bash
docker exec coffeprince-redis redis-cli KEYS '*'         # menus::all
docker exec coffeprince-redis redis-cli GET menus::all   # 메뉴 목록 JSON
docker exec coffeprince-redis redis-cli TTL menus::all   # 남은 초
```
- `GET /api/v1/menus`를 두 번 호출하면 SQL 로그가 첫 번째에만 찍힙니다.
- 메뉴 변경 후 다시 조회하면 변경 결과가 보입니다.



📌 [Redis 메뉴 캐시 주의사항 (#43)]

✅ 앱 실행 전, 아래 둘 중 하나만 되어 있으면 OK
  1) Redis 켜기 (최초 1회)
     → docker compose up -d redis
  2) 캐시 끄기 (Redis 안 쓸 때)
     → local.env 에 CACHE_ENABLED=false 한 줄 추가

❌ 둘 다 안 하면?
  → 앱은 켜지지만 메뉴 목록(A06) 조회 시 500 에러

⚠️ 이런 경우도 500 에러
  - Docker Desktop이 꺼져 있음 → Docker Desktop 켜기
  - 6379 포트를 다른 프로그램이 사용 중 → 해당 프로그램 종료

💡 참고
  - Docker가 꼭 필요한 건 아님 (Redis만 6379 포트에 켜져 있으면 됨)
  - Docker로 한 번 띄우면 이후엔 Docker Desktop만 켜져 있으면 자동 실행

## RestClient 외부 게시글 조회

`GET /api/v1/reference/posts/{postId}` → 외부 서버의 `/posts/{postId}`를 조회합니다. (화면 연결 없음)

| 항목 | 내용 |
|---|---|
| 외부 주소 | 기본 `https://jsonplaceholder.typicode.com` · 환경 변수 `REFERENCE_BASE_URL`로 변경 |
| 시간 제한 | 연결 3초 · 응답 읽기 5초 |
| 오류 | 400 입력 오류 · 404 외부 게시글 없음 · 502 외부 서버 오류/연결 실패 · 504 시간 초과 |
| 코드 | `ReferencePostController`, `ReferencePostService` |

> **jsonplaceholder**: 명세가 지정한 연습용 공개 API (가짜 게시글 100개 제공)

**테스트** (7가지 경우, 약 10초)
```bash
./gradlew test --tests "*ReferencePostControllerTest"
```
> 테스트 중에만 **가짜 외부 서버**를 띄워 404·500·연결 실패·시간 초과를 재현합니다. 실제 jsonplaceholder에는 오류를 일으키지 않습니다.
