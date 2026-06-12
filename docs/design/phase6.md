# Phase 6 — 모니터링 & 출고 처리

## 목표

메뉴 `[4] 모니터링`과 메뉴 `[6] 출고 처리` 기능을 완성한다.

- **모니터링**: 서브 메뉴로 ① 상태별 주문 건수 집계, ② 시료별 재고 현황(상태 + 잔여율) 제공
- **출고 처리**: `CONFIRMED` 주문 목록 조회 → 번호 입력 → `RELEASE` 전환

## 사전 확인 (이미 구현됨)

| 항목 | 파일 | 비고 |
|------|------|------|
| `OrderRepository.findByStatus()` | `repository/OrderRepository.java` | 상태별 조회 |
| `SampleRepository.findAll()` | `repository/SampleRepository.java` | 전체 시료 조회 |
| `Order.setStatus()` | `model/Order.java` | 상태 전환에 재사용 |

## 변경 파일 목록

```
src/main/java/org/example/
├── model/
│   └── StockLevel.java                ← 신규: SURPLUS / SHORTAGE / DEPLETED
├── controller/
│   ├── MonitoringController.java      ← 신규
│   ├── ReleaseController.java         ← 신규
│   └── MainController.java            ← 수정: case "4", "6"
├── view/
│   ├── MonitoringInputView.java       ← 신규: 서브 메뉴 입력
│   ├── MonitoringOutputView.java      ← 신규
│   ├── ReleaseInputView.java          ← 신규: 출고할 번호 입력
│   └── ReleaseOutputView.java         ← 신규
└── Application.java                   ← 수정: 신규 Controller 조립

src/test/java/org/example/
├── controller/
│   ├── MonitoringControllerTest.java  ← 신규
│   └── ReleaseControllerTest.java     ← 신규
└── view/
    ├── MonitoringOutputViewTest.java  ← 신규
    └── ReleaseOutputViewTest.java     ← 신규
```

## 클래스 설계

### StockLevel.java

```java
public enum StockLevel {
    SURPLUS,   // 여유: 재고 > 0, 재고 >= RESERVED 주문 수량 합
    SHORTAGE,  // 부족: 재고 > 0, 재고 < RESERVED 주문 수량 합
    DEPLETED   // 고갈: 재고 == 0
}
```

### MonitoringController.java

```java
public class MonitoringController {
    // 생성자 주입: SampleRepository, OrderRepository,
    //              MonitoringInputView, MonitoringOutputView

    public void run()
    // 서브 메뉴 루프:
    //   [1] 주문량 확인 → showOrderCounts()
    //   [2] 재고량 확인 → showStockStatus()
    //   [0] 뒤로       → 복귀
}
```

**재고 상태 판정 로직:**

```
reservedQty = RESERVED 상태 주문 중 해당 시료 ID의 quantity 합산

if stock == 0            → DEPLETED
if stock < reservedQty   → SHORTAGE
else                     → SURPLUS  (reservedQty == 0인 경우 포함)
```

**잔여율 계산:**

```
대기 수요 = RESERVED 총량 + PRODUCING 총량
잔여율(%) = min(stock / 대기 수요, 1.0) * 100
           대기 수요 == 0이고 stock > 0 이면 100%
           대기 수요 == 0이고 stock == 0 이면 0%
```

**집계 대상 상태:** `RESERVED` / `PRODUCING` / `CONFIRMED` / `RELEASE`
**집계 제외:** `REJECTED`

### MonitoringInputView.java

```java
public String readSubMenuSelection()  // "[1] 주문량 확인  [2] 재고량 확인  [0] 뒤로" 프롬프트 포함
```

### MonitoringOutputView.java

```java
public void showOrderCounts(Map<OrderStatus, Long> counts)
// 상태별 주문 건수 출력 (REJECTED 제외)

public void showStockStatus(List<Sample> samples,
                            Map<String, StockLevel> levels,
                            Map<String, Double> remainingRates)
// 시료별 재고 · 상태 · 잔여율 출력
```

### ReleaseController.java

```java
public class ReleaseController {
    // 생성자 주입: OrderRepository, ReleaseInputView, ReleaseOutputView

    public void run()
    // 1. CONFIRMED 주문 목록 조회
    // 2. 없으면 안내 메시지 후 복귀
    // 3. 번호 목록 출력 후 "출고할 번호" 입력 (0: 취소)
    // 4. "0" → 취소 복귀
    // 5. 유효 번호 → RELEASE 전환 + 완료 메시지 (주문번호·수량·처리일시·상태)
}
```

### ReleaseInputView.java

```java
public int readSelectionNumber()  // "출고할 번호 > " 프롬프트, 정수 반환
```

### ReleaseOutputView.java

```java
public void showConfirmedList(List<Order> orders)  // 번호·주문번호·고객·수량 테이블
public void showNoConfirmedOrders()                // 출고 대기 없음 메시지
public void showReleased(Order order, LocalDateTime processedAt)
// 완료: 주문번호·출고수량·처리일시·상태(CONFIRMED→RELEASE)
```

## 화면 출력 형식

### 모니터링 — 서브 메뉴

```
=== 모니터링 ===
[1] 주문량 확인    [2] 재고량 확인    [0] 뒤로
선택 > _
```

### 모니터링 — [1] 주문량 확인

```
[상태별 주문 현황]
  RESERVED  :  3건
  PRODUCING :  3건  ← 생산라인 대기
  CONFIRMED :  8건
  RELEASE   : 18건
```

### 모니터링 — [2] 재고량 확인

```
[재고 현황]
  시료명                   재고      상태    잔여율
  실리콘 웨이퍼-8인치     480 ea    여유    [########--]  80%
  GaN 에피택셀-4인치      220 ea    여유    [####------]  44%
  SiC 파워기판-6인치       30 ea    부족    [-----------]   6%
  산화막 웨이퍼-SiO2        0 ea    고갈    [-----------]   0%
```

### 출고 처리

```
=== 출고 처리 ===

[출고 가능 주문 (CONFIRMED)]
  번호  주문번호              고객          시료               수량
  [1]   ORD-20260416-0042   SK하이닉스    실리콘 웨이퍼-8인치  150 ea
  [2]   ORD-20260416-0035   DB하이텍      포토레지스트-PR7     400 ea

출고할 번호 > 1

출고 처리 완료.
  주문번호  ORD-20260416-0042
  출고수량  150 ea
  처리일시  2026-04-16 09:34:02
  상태      CONFIRMED → RELEASE
```

### 출고 대기 없을 때

```
=== 출고 처리 ===
출고 대기 중인 주문이 없습니다.
```

## 비즈니스 규칙

- 모니터링 서브 메뉴는 `0` 입력 시 메인 메뉴로 복귀
- 모니터링에서 `REJECTED` 주문은 건수 집계에서 제외
- 재고 상태 판정 및 잔여율 기준: 현재 재고 vs RESERVED 주문 수량 합
- 출고 선택은 **번호(1, 2, …)** 입력 — 주문번호 문자열 직접 입력 아님
- `0` 입력 시 취소 복귀 (상태 변경 없음)
- 완료 메시지에 처리일시(`LocalDateTime.now()`) 포함

## MainController / Application 수정 내용

```java
// MainController
case "4" -> monitoringController.run();
case "6" -> releaseController.run();

// Application.java
MonitoringController monitoringController = new MonitoringController(
    sampleRepository, orderRepository,
    new MonitoringInputView(scanner), new MonitoringOutputView(System.out));

ReleaseController releaseController = new ReleaseController(
    orderRepository,
    new ReleaseInputView(scanner), new ReleaseOutputView(System.out));
```

## TDD 사이클 계획

### 사이클 1 — MonitoringController: 주문 상태별 건수 집계 (REJECTED 제외)

- **테스트 이름**: `showOrderCounts_excludesRejected`
- **@DisplayName**: `[1] 선택 시 상태별 주문 건수를 출력하며 REJECTED는 제외된다`
- **Given**: RESERVED 2건, PRODUCING 1건, REJECTED 1건 주문, 서브 메뉴 입력 "1\n0\n"
- **When**: `run()` 호출
- **Then**: 출력에 RESERVED "2", PRODUCING "1" 포함 / REJECTED 건수 미포함

### 사이클 2 — MonitoringController: 재고 고갈 판정

- **테스트 이름**: `showStockStatus_withZeroStock_showsDepleted`
- **@DisplayName**: `[2] 선택 시 재고가 0인 시료는 고갈로 표시된다`
- **Given**: 재고 0인 시료, 서브 메뉴 입력 "2\n0\n"
- **When**: `run()` 호출
- **Then**: 출력에 "고갈" 포함

### 사이클 3 — MonitoringController: 재고 부족 판정

- **테스트 이름**: `showStockStatus_withInsufficientStock_showsShortage`
- **@DisplayName**: `RESERVED 주문 수량 합이 재고를 초과하면 부족으로 표시된다`
- **Given**: 재고 5인 시료, 해당 시료 RESERVED 주문 수량 합 10, 서브 메뉴 "2\n0\n"
- **When**: `run()` 호출
- **Then**: 출력에 "부족" 포함

### 사이클 4 — MonitoringController: 재고 여유 판정

- **테스트 이름**: `showStockStatus_withSufficientStock_showsSurplus`
- **@DisplayName**: `재고가 RESERVED 주문 수량 이상이면 여유로 표시된다`
- **Given**: 재고 20인 시료, 해당 시료 RESERVED 주문 수량 합 10, 서브 메뉴 "2\n0\n"
- **When**: `run()` 호출
- **Then**: 출력에 "여유" 포함

### 사이클 5 — ReleaseController: CONFIRMED 없을 때 안내 메시지

- **테스트 이름**: `run_withNoConfirmedOrders_showsEmptyMessage`
- **@DisplayName**: `CONFIRMED 주문이 없을 때 안내 메시지를 출력하고 복귀한다`
- **Given**: CONFIRMED 주문 없음
- **When**: `run()` 호출
- **Then**: 안내 메시지 출력, 추가 입력 없이 복귀

### 사이클 6 — ReleaseController: 번호 입력으로 출고 처리

- **테스트 이름**: `run_withValidNumber_releasesOrder`
- **@DisplayName**: `유효한 번호 입력 시 주문 상태가 RELEASE로 전환된다`
- **Given**: CONFIRMED 주문 1건, 입력 "1"
- **When**: `run()` 호출
- **Then**: 주문 상태 == `RELEASE`

### 사이클 7 — ReleaseController: 취소(0) 입력 시 상태 변경 없음

- **테스트 이름**: `run_withCancelInput_doesNotChangeStatus`
- **@DisplayName**: `0 입력 시 주문 상태가 변경되지 않는다`
- **Given**: CONFIRMED 주문 있음, 입력 "0"
- **When**: `run()` 호출
- **Then**: 주문 상태 == `CONFIRMED`

### 사이클 8 — MonitoringOutputView: 주문 건수 + 재고 현황 출력

- **테스트 이름**: `showOrderCounts_containsStatusAndCount` / `showStockStatus_containsStockLevelAndRate`
- **@DisplayName**: `출력에 주문 건수와 시료별 재고 상태·잔여율이 포함된다`

### 사이클 9 — ReleaseOutputView: 완료 메시지에 처리일시 포함

- **테스트 이름**: `showReleased_containsOrderInfoAndProcessedAt`
- **@DisplayName**: `완료 메시지에 주문번호·출고수량·처리일시가 포함된다`
