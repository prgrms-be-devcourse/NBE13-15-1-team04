# coffe-prince

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

## 선택 심화: Redis 메뉴 조회 캐시

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