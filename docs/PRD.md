# PRD — 반도체 시료 생산주문관리 시스템

## 배경

가상의 반도체 회사 S-Semi는 시료(Sample)를 생산해 연구소·팹리스 업체·대학 연구실에 납품한다.
주문량 급증으로 엑셀/메모장 관리의 한계가 드러나, 콘솔 기반 생산주문관리 시스템을 개발한다.

## 역할

| 역할 | 책임 |
|------|------|
| 주문 담당자 | 고객 요청을 받아 주문서 작성, 시료 관리 |
| 생산 담당자 | 주문 수신 후 승인 또는 거절, 시료 등록 |

## 메인 메뉴

메뉴 진입 시마다 시스템 현황을 표시한다.

| 항목 | 내용 |
|------|------|
| 시스템 현황 | 현재 시각 |
| 등록 시료 | 등록된 시료 종 수 |
| 총 재고 | 전체 시료 재고 합계 |
| 전체 주문 | REJECTED 제외 전체 주문 수 |
| 생산라인 | PRODUCING 상태 주문 수 (대기 수) |

| 번호 | 메뉴 | 기능 |
|------|------|------|
| 1 | 시료 관리 | 시료 등록 / 목록 조회(재고 포함) / 이름 검색 |
| 2 | 시료 주문 | 주문 생성 → RESERVED 상태로 접수 |
| 3 | 주문 승인/거절 | RESERVED 목록 조회 후 승인 또는 거절 |
| 4 | 모니터링 | 상태별 주문 수 / 시료별 재고 및 상태 |
| 5 | 생산라인 조회 | 진행률(%) · 완료 예정 시각 · 대기 큐(FIFO) — 시간 경과 시 자동 완료 |
| 6 | 출고 처리 | CONFIRMED 주문 선택 후 RELEASE로 전환 |

## 도메인 모델

### Sample (시료)

| 속성 | 설명 |
|------|------|
| 시료 ID | 고유 식별자 (예: S-001) |
| 이름 | 시료명 |
| 평균 생산시간 | min/ea |
| 수율 | 정상품 비율 (0~1). 예: 100개 중 90개 정상 = 0.9 |

시스템에 등록된 시료만 주문 가능하다.

### Order (주문)

- **주문번호 형식**: `ORD-YYYYMMDD-NNNN`
- **입력값**: 시료 ID, 고객명, 주문 수량

### 주문 상태 흐름

```
RESERVED → (승인 + 재고 충분) → CONFIRMED → RELEASE
         → (승인 + 재고 부족) → PRODUCING → CONFIRMED → RELEASE
         → (거절)             → REJECTED
```

| 상태 | 의미 |
|------|------|
| RESERVED | 주문 접수 |
| REJECTED | 주문 거절 (모니터링에서 제외) |
| PRODUCING | 승인 완료, 재고 부족으로 생산 중 |
| CONFIRMED | 승인 완료, 출고 대기 |
| RELEASE | 출고 완료 |

## 핵심 비즈니스 규칙

### 주문 승인 처리

승인 시 재고를 자동 확인하여 두 경로로 분기:
1. **재고 충분** → 즉시 `CONFIRMED`
2. **재고 부족** → 부족분만큼 생산 라인에 자동 등록, `PRODUCING`으로 전환

### 생산 라인

- 단일 라인, 시료를 하나씩 생산 / 스케줄링: **FIFO**
- **실 생산량** = `ceil(부족분 / (수율 * 0.9))`
- **총 생산 시간** = `평균 생산시간 * 실 생산량`
- 생산 완료 시 주문 상태: `PRODUCING → CONFIRMED`, 재고 증가

### 재고 상태

| 상태 | 조건 |
|------|------|
| 여유 | 주문 대비 재고 충분 |
| 부족 | 주문 대비 재고 부족 |
| 고갈 | 재고 수량 = 0 |

**잔여율** = `min(현재 재고 / 대기 수요 총량, 1.0) × 100`

- 대기 수요 = RESERVED 총량 + PRODUCING 총량 (아직 출고되지 않은 모든 주문)
- 대기 수요 없음 + 재고 > 0 → 100%
- 대기 수요 없음 + 재고 = 0 → 0%
- 재고 ≥ 대기 수요 → 100% (여유)
- 재고 < 대기 수요 → 재고/대기 수요 × 100 (부족)

## 아키텍처

### 패키지 구조

```
src/main/java/org/example/
├── Application.java          # 진입점 — 의존성 조립 후 Controller 주입
├── model/                    # 도메인 객체 (Sample, Order, ProductionLine 등)
├── controller/               # 비즈니스 로직
├── view/                     # InputView / OutputView
└── repository/               # Repository 인터페이스 + RepositoryFactory
    └── impl/                 # File / Json / Database 구현체
```

데이터 파일은 `data/` 디렉토리에 저장한다.

### PoC에서 가져온 구현 패턴

**ConsoleMVC** — `Application.java`에서 의존성을 직접 조립해 Controller에 주입한다. Controller의 `run()` 루프는 `switch`로 메뉴 번호를 분기한다.

```java
SampleRepository repo = RepositoryFactory.create(PersistenceType.DATABASE);
InputView in = new InputView();
OutputView out = new OutputView();
new MainController(repo, in, out).run();
```

**DataPersistence** — Repository를 인터페이스로 추상화하고 `RepositoryFactory`의 `PersistenceType` enum(`FILE` / `JSON` / `DATABASE`)으로 구현체를 교체한다. 구현체 3종은 `repository/impl/` 하위에 둔다.

**DataMonitor** — 모니터링은 REPL + Command 패턴으로 구현한다. `Map<String, Command>`에 커맨드를 등록하고 입력 토큰으로 조회·실행한다.

**DummyDataGenerator** — CLI 인수 `--tables=all --count=50` 형식. `DatabaseConfig`로 H2 스키마를 초기화하고, `GeneratorRegistry`에 등록된 `DataGenerator` 구현체가 테이블별로 데이터를 삽입한다.

## 평가 주안점

1. CLAUDE.md, PRD.md 문서 관리
2. Harness(테스트 하네스) 도입
3. 테스트 커버리지
4. Clean Code
5. Commit 이력 관리
