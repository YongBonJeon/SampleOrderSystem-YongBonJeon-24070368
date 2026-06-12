# PLAN.md — 개발 Phase 계획

## Phase 구성 요약

| Phase | 내용 | 상태 |
|-------|------|------|
| 1 | 프로젝트 기반 구조 | ✅ 완료 |
| 2 | 시료(Sample) 관리 | ✅ 완료 |
| 3 | 주문(Order) 접수 | ✅ 완료 |
| 4 | 주문 승인 / 거절 | ⬜ 대기 |
| 5 | 생산 라인 | ⬜ 대기 |
| 6 | 모니터링 & 출고 처리 | ⬜ 대기 |
| 7 | 데이터 영속성 | ⬜ 대기 |

---

## Phase 1 — 프로젝트 기반 구조

**목표**: 이후 모든 Phase가 올라갈 골격을 완성한다.

### 작업 항목

- `build.gradle`에 의존성 추가 (H2, Gson)
- 패키지 구조 생성 (`model`, `controller`, `view`, `repository`, `repository/impl`)
- `Application.java` — 의존성 조립 및 Controller 주입 진입점
- `MainController` — `run()` 루프 + 메뉴 번호 `switch` 골격 (6개 메뉴 stub)
- `InputView` / `OutputView` — 공통 입출력 유틸
- 각 Repository 인터페이스 선언 (`SampleRepository`, `OrderRepository`)
- InMemory 구현체 (`InMemorySampleRepository`, `InMemoryOrderRepository`)

### 완료 기준

- `./gradlew run` 실행 시 메인 메뉴가 출력되고 번호 입력으로 분기되어야 한다.
- 모든 메뉴는 "미구현" 메시지를 출력해도 무방하다.

---

## Phase 2 — 시료(Sample) 관리

**목표**: 메뉴 [1] 시료 관리의 전체 기능을 완성한다.

**선행 조건**: Phase 1 완료

### 작업 항목

- `Sample` 도메인 모델 (시료 ID, 이름, 평균 생산시간, 수율, 현재 재고)
- `SampleRepository` CRUD 구현 (`InMemorySampleRepository`)
- `SampleController` — 시료 등록 / 목록 조회 / 이름 검색
- View — 시료 목록 테이블 출력 (ID, 이름, 평균 생산시간, 수율, 현재 재고)

### 완료 기준

- 시료를 등록하고 목록에서 재고와 함께 조회할 수 있다.
- 이름 키워드로 시료를 검색할 수 있다.
- 존재하지 않는 시료 ID 입력 시 오류 메시지가 출력된다.

---

## Phase 3 — 주문(Order) 접수

**목표**: 메뉴 [2] 시료 주문 기능을 완성한다.

**선행 조건**: Phase 2 완료 (등록된 시료가 있어야 주문 가능)

### 작업 항목

- `OrderStatus` enum (`RESERVED`, `REJECTED`, `PRODUCING`, `CONFIRMED`, `RELEASE`)
- `Order` 도메인 모델 (주문번호, 시료 ID, 고객명, 주문 수량, 상태, 접수일시)
- 주문번호 생성 유틸 — `ORD-YYYYMMDD-NNNN` 형식
- `OrderRepository` CRUD 구현 (`InMemoryOrderRepository`)
- `OrderController` — 주문 생성 → `RESERVED` 상태로 저장
- View — 입력 확인 화면 + 접수 완료 출력 (주문번호, 현재 상태)

### 완료 기준

- 시료 ID / 고객명 / 수량 입력 후 확인 절차를 거쳐 주문이 `RESERVED`로 등록된다.
- 등록되지 않은 시료 ID 입력 시 오류 처리된다.

---

## Phase 4 — 주문 승인 / 거절

**목표**: 메뉴 [3] 주문 승인/거절 기능을 완성한다.

**선행 조건**: Phase 3 완료

### 작업 항목

- `RESERVED` 상태 주문 목록 조회
- **승인 로직**
  - 재고 ≥ 주문 수량 → 재고 차감, 상태 `CONFIRMED`
  - 재고 < 주문 수량 → 생산 큐에 등록, 상태 `PRODUCING`
- **거절 로직** — 즉시 `REJECTED`
- `ProductionQueue` — 생산 작업 대기열 (FIFO, Phase 5와 공유)
- View — 재고 확인 결과 및 상태 전환 출력

### 완료 기준

- 재고가 충분한 주문 승인 시 `CONFIRMED`로 전환되고 재고가 차감된다.
- 재고가 부족한 주문 승인 시 `PRODUCING`으로 전환되고 생산 큐에 등록된다.
- 거절 시 즉시 `REJECTED`로 전환된다.

---

## Phase 5 — 생산 라인

**목표**: 메뉴 [5] 생산라인 조회 기능을 완성하고 생산 완료 처리를 구현한다.

**선행 조건**: Phase 4 완료 (생산 큐에 작업이 쌓여야 함)

### 작업 항목

- `ProductionJob` 도메인 모델 (연결된 주문, 시료, 부족분, 실 생산량, 총 생산 시간)
- **실 생산량 공식**: `ceil(부족분 / (수율 * 0.9))`
- **총 생산 시간**: `평균 생산시간 * 실 생산량`
- `ProductionLine` — FIFO 큐 관리, 현재 처리 중인 작업 추적
- 생산 완료 처리 — 재고 증가, 주문 상태 `PRODUCING → CONFIRMED`
- View — 현재 생산 중인 작업 정보 + 대기 큐 목록 출력

### 완료 기준

- 생산라인 조회 시 현재 처리 중인 작업과 대기 큐가 FIFO 순서로 출력된다.
- 생산 완료 처리 후 재고가 증가하고 주문 상태가 `CONFIRMED`로 전환된다.

---

## Phase 6 — 모니터링 & 출고 처리

**목표**: 메뉴 [4] 모니터링과 메뉴 [6] 출고 처리 기능을 완성한다.

**선행 조건**: Phase 5 완료

### 작업 항목

**모니터링**
- 상태별 주문 수 집계 (`RESERVED` / `CONFIRMED` / `PRODUCING` / `RELEASE`, `REJECTED` 제외)
- 시료별 재고 현황 및 재고 상태 판정
  - 여유: 주문 대비 재고 충분
  - 부족: 주문 대비 재고 부족
  - 고갈: 재고 = 0

**출고 처리**
- `CONFIRMED` 상태 주문 목록 조회
- 선택한 주문 → `RELEASE`로 전환

### 완료 기준

- 모니터링 화면에서 상태별 주문 수와 시료별 재고 상태(여유/부족/고갈)가 정확히 표시된다.
- `CONFIRMED` 주문을 선택해 출고 처리하면 `RELEASE`로 전환된다.

---

## Phase 7 — 데이터 영속성

**목표**: 애플리케이션 재시작 후에도 데이터가 유지되도록 영속성 계층을 완성한다.

**선행 조건**: Phase 6 완료 (모든 기능이 InMemory로 동작 확인 후 교체)

### 작업 항목

- `RepositoryFactory` + `PersistenceType` enum (`FILE` / `JSON` / `DATABASE`)
- `FileRepository` 구현체 — 직렬화/역직렬화, `data/*.dat`
- `JsonRepository` 구현체 — Gson 사용, `data/*.json`
- `DatabaseRepository` 구현체 — H2 JDBC, `data/` 디렉토리
- `DatabaseConfig` — H2 스키마 초기화
- `DummyDataGenerator` — `--tables=all --count=N` CLI 인수, 테스트용 더미 데이터 삽입

### 완료 기준

- `PersistenceType`을 바꿔도 동일하게 동작한다.
- 애플리케이션 재시작 후 이전 데이터가 유지된다.
- DummyDataGenerator 실행 후 시료·주문 데이터가 DB에 삽입된다.
