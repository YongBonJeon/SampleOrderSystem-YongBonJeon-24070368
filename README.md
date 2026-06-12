# 반도체 시료 생산주문관리 시스템

가상의 반도체 회사 S-Semi의 시료(Sample) 생산 및 주문을 관리하는 콘솔 기반 Java 애플리케이션.

## 요구 사항

- Java 17 이상
- Gradle (Wrapper 포함 — `./gradlew` 사용 가능)

## 빠른 시작

```bash
# 빌드
./gradlew build

# 실행 (기본: FILE 영속성)
./gradlew run
```

실행하면 메인 메뉴가 표시됩니다.

```
============================================================
  반도체 시료 생산주문관리 시스템
============================================================
  시스템 현황  2026-06-12 14:23:01

  등록 시료  3종    총 재고  1,200 ea
  전체 주문  7건    생산라인  1건 대기

  [1] 시료 관리       [2] 시료 주문
  [3] 주문 승인/거절  [4] 모니터링
  [5] 생산라인 조회   [6] 출고 처리
  [0] 종료
선택 >
```

## 메뉴 구성

| 번호 | 메뉴 | 기능 |
|------|------|------|
| 1 | 시료 관리 | 시료 등록 / 목록 조회 / 이름 검색 |
| 2 | 시료 주문 | 주문 생성 → RESERVED 상태로 접수 |
| 3 | 주문 승인/거절 | RESERVED 목록 번호 선택 → 재고 확인 → 승인(Y) 또는 거절(N) |
| 4 | 모니터링 | 상태별 주문 건수 / 시료별 재고 현황 및 잔여율 |
| 5 | 생산라인 조회 | 진행률(%) · 완료 예정 시각 · 대기 큐 — 완료 시 자동 전환 |
| 6 | 출고 처리 | CONFIRMED 주문 번호 선택 → RELEASE 전환 |

## 주문 상태 흐름

```
RESERVED → (승인 + 재고 충분) → CONFIRMED → RELEASE
         → (승인 + 재고 부족) → PRODUCING → CONFIRMED → RELEASE
         → (거절)             → REJECTED
```

## 영속성 설정

`-Dpersistence=<TYPE>` JVM 옵션으로 저장 방식을 선택합니다. 기본값은 `FILE`.

| 타입 | 설명 | 저장 위치 |
|------|------|-----------|
| `MEMORY` | 재시작 시 초기화 | — |
| `FILE` | Java 직렬화 | `data/*.dat` |
| `JSON` | JSON 파일 | `data/*.json` |
| `DATABASE` | H2 임베디드 DB | `data/orders.mv.db` |

```bash
# DATABASE 모드로 실행
./gradlew run -Dpersistence=DATABASE

# JSON 모드로 실행
./gradlew run -Dpersistence=JSON
```

## 더미 데이터 생성 (DATABASE 모드)

```bash
# 시료 10건 + 주문 10건 생성
./gradlew run --args="--tables=all --count=10" -PmainClass=org.example.generator.DummyDataGenerator
```

옵션:

| 옵션 | 기본값 | 설명 |
|------|--------|------|
| `--tables=all` | `all` | 삽입할 테이블 (`samples` / `orders` / `all`) |
| `--count=N` | `10` | 테이블당 삽입할 행 수 |

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 단일 클래스
./gradlew test --tests "org.example.controller.ApprovalControllerTest"

# 단일 메서드
./gradlew test --tests "org.example.controller.ApprovalControllerTest.approve_withSufficientStock_confirmsOrder"

# 테스트 리포트
# build/reports/tests/test/index.html
```

## 프로젝트 구조

```
src/main/java/org/example/
├── Application.java              # 진입점 — 의존성 조립
├── model/                        # 도메인 객체 (Sample, Order, ProductionJob …)
├── controller/                   # 비즈니스 로직
├── view/                         # InputView / OutputView
├── queue/                        # ProductionQueue (FIFO)
├── repository/                   # Repository 인터페이스
│   └── impl/                     # InMemory / File / Json / Database 구현체
├── persistence/                  # RepositoryFactory, PersistenceType, DatabaseConfig
├── util/                         # OrderIdGenerator
└── generator/                    # DummyDataGenerator

docs/
├── PRD.md                        # 기능 명세 및 비즈니스 규칙
├── PLAN.md                       # Phase별 개발 계획
├── REVIEW.md                     # 코드 리뷰 보고서
└── design/                       # Phase별 설계 문서 (phase1.md … phase7.md)
```

## 핵심 비즈니스 규칙

**실 생산량** = `ceil(부족분 / (수율 × 0.9))`  
수율 손실과 안전계수 0.9를 반영해 필요량보다 여유 있게 생산한다.

**총 생산시간** = `평균 생산시간(min/ea) × 실 생산량`  
생산라인 조회 시 경과 시간이 총 생산시간을 초과하면 자동으로 CONFIRMED 전환.

**잔여율** = `min(현재 재고 / 대기 수요 총량, 1.0) × 100`  
대기 수요 = RESERVED 총량 + PRODUCING 총량
