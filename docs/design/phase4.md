# Phase 4 — 주문 승인 / 거절

## 목표

메뉴 `[3] 주문 승인/거절` 기능을 완성한다.  
`RESERVED` 주문 목록을 조회하고, 승인(재고 분기) 또는 거절 처리를 수행한다.

## 사전 확인 (이미 구현됨)

| 항목 | 파일 | 비고 |
|------|------|------|
| `Order` / `OrderStatus` | `model/Order.java`, `model/OrderStatus.java` | 완료 |
| `Sample` (재고 포함) | `model/Sample.java` | `stock`, `setStock()` 완료 |
| `OrderRepository` | `repository/OrderRepository.java` | `findByStatus()` 완료 |
| `SampleRepository` | `repository/SampleRepository.java` | `findById()`, `update()` 완료 |
| `InMemoryOrderRepository` | `repository/impl/InMemoryOrderRepository.java` | 완료 |
| `InMemorySampleRepository` | `repository/impl/InMemorySampleRepository.java` | 완료 |

Phase 4에서 **신규 작성**할 대상: `ProductionJob`, `ProductionQueue`, `ApprovalController`, `ApprovalInputView`, `ApprovalOutputView`, `MainController` 수정.

## 변경 파일 목록

```
src/main/java/org/example/
├── model/
│   └── ProductionJob.java             ← 신규: 생산 작업 도메인 모델
├── queue/
│   └── ProductionQueue.java           ← 신규: FIFO 생산 대기열 (Phase 5와 공유)
├── controller/
│   ├── ApprovalController.java        ← 신규: 승인/거절 흐름
│   └── MainController.java            ← 수정: case "3" → ApprovalController 위임
└── view/
    ├── ApprovalInputView.java         ← 신규: 승인/거절 입력
    └── ApprovalOutputView.java        ← 신규: 승인/거절 출력

src/test/java/org/example/
├── model/
│   └── ProductionJobTest.java         ← 신규: 실 생산량·총 생산시간 계산 검증
├── queue/
│   └── ProductionQueueTest.java       ← 신규: FIFO 순서 검증
├── controller/
│   └── ApprovalControllerTest.java    ← 신규
└── view/
    └── ApprovalOutputViewTest.java    ← 신규
```

## 클래스 설계

### ProductionJob.java

```java
public class ProductionJob {
    private final String orderId;
    private final String sampleId;
    private final int shortage;       // 부족분 (주문 수량 - 재고)
    private final int actualQty;      // 실 생산량
    private final double totalTime;   // 총 생산시간 (min)

    // 실 생산량 = ceil(부족분 / (수율 × 0.9))
    // 총 생산시간 = 평균 생산시간 × 실 생산량
    public ProductionJob(String orderId, String sampleId, int shortage,
                         double yieldRate, double avgProductionTime)

    public String getOrderId()
    public String getSampleId()
    public int getShortage()
    public int getActualQty()
    public double getTotalTime()
}
```

### ProductionQueue.java

```java
public class ProductionQueue {
    // 내부: LinkedList<ProductionJob> — FIFO

    public void enqueue(ProductionJob job)
    public Optional<ProductionJob> peek()   // 현재 처리 중인 작업 (제거 안 함)
    public List<ProductionJob> getWaiting() // peek() 이후 대기 중인 작업 목록
    public boolean isEmpty()
}
```

> Phase 5에서 `dequeue()` 및 생산 완료 처리를 추가한다.

### ApprovalController.java

```java
public class ApprovalController {
    // 생성자 주입: SampleRepository, OrderRepository,
    //              ProductionQueue, ApprovalInputView, ApprovalOutputView

    public void run() {
        // RESERVED 목록 조회 → 비어있으면 안내 후 복귀
        // 목록 출력 → 번호 입력 (0: 취소) → 재고 현황 표시 → 승인(Y) 또는 거절(N) 선택
        // approve() 또는 reject() 분기
    }

    private void approve(Order order, Sample sample)
    // 재고 충분: 재고 차감 → CONFIRMED
    // 재고 부족: ProductionQueue 등록 → PRODUCING

    private void reject(Order order)
    // 즉시 REJECTED
}
```

**`approve()` 상세 흐름**:

```
재고 >= 주문 수량
  → sample.setStock(stock - quantity)
  → sampleRepository.update(sample)
  → order.setStatus(CONFIRMED)
  → orderRepository.update(order)

재고 < 주문 수량
  → shortage = quantity - stock
  → new ProductionJob(orderId, sampleId, shortage, yieldRate, avgProductionTime)
  → productionQueue.enqueue(job)
  → order.setStatus(PRODUCING)
  → orderRepository.update(order)
```

### ApprovalInputView.java

```java
public class ApprovalInputView {
    // 생성자: Scanner 주입

    public int readSelectionNumber()  // "승인할 번호 (0: 취소) > " 프롬프트, 정수 반환
    public String readDecision()      // Y(승인) / N(거절) 입력
}
```

### ApprovalOutputView.java

```java
public class ApprovalOutputView {
    // 생성자: PrintStream 주입

    public void showReservedList(List<Order> orders, Map<String, String> sampleNames)
    // RESERVED 주문 목록 출력 (번호·주문번호·고객·시료명·수량·상태 컬럼)

    public void showNoReservedOrders()
    // 대기 주문 없음 메시지

    public void showInvalidSelection()
    // 범위 외 번호 입력 오류

    public void showStockInfo(int stock, int orderQty)
    // 재고 현황: 현재 재고 / 주문 수량 / 충분·부족 표시

    public void showConfirmed(Order order)
    // 재고 충분 → CONFIRMED 전환 결과

    public void showProducing(Order order, ProductionJob job)
    // 재고 부족 → PRODUCING 전환 + 생산 등록 결과

    public void showRejected(Order order)
    // REJECTED 전환 결과
}
```

## 화면 출력 형식

### RESERVED 목록

```
승인 대기 중인 예약 목록  (RESERVED)
------------------------------------------------------------------------
 번호  주문번호              고객              시료                수량     상태
 [1]   ORD-20260612-0001    한국반도체연구소  산화막 웨이퍼-SiO2   200 ea   RESERVED
 [2]   ORD-20260612-0002    서울대학교        실리콘 웨이퍼-8인치   50 ea   RESERVED
------------------------------------------------------------------------
승인할 번호 (0: 취소) > 1
재고 현황: 현재 재고 480 ea  /  주문 수량 200 ea  →  재고 충분
승인(Y) / 거절(N) > Y
```

### 재고 충분 → CONFIRMED

```
재고 현황: 현재 재고 480 ea  /  주문 수량 200 ea  →  재고 충분
주문 ORD-20260612-0001 승인 완료 → CONFIRMED
```

### 재고 부족 → PRODUCING

```
재고 현황: 현재 재고 30 ea  /  주문 수량 200 ea  →  재고 부족
생산 등록: 실 생산량 210 ea / 총 생산시간 105.0 min
주문 ORD-20260612-0001 승인 완료 → PRODUCING
```

### 거절

```
승인할 번호 (0: 취소) > 1
재고 현황: 현재 재고 480 ea  /  주문 수량 200 ea  →  재고 충분
승인(Y) / 거절(N) > N
주문 ORD-20260612-0001 거절 완료 → REJECTED
```

### 0 입력 시 취소

```
승인할 번호 (0: 취소) > 0
(메인 메뉴로 복귀)
```

## 비즈니스 규칙

- 승인/거절 대상은 `RESERVED` 상태 주문만 가능
- **실 생산량** = `ceil(부족분 / (수율 × 0.9))`
- **총 생산시간** = `평균 생산시간 × 실 생산량`
- `CONFIRMED` 전환 시에만 재고를 차감한다 (`PRODUCING`은 차감 안 함 — Phase 5에서 생산 완료 후 재고 증가)
- `REJECTED` 주문은 이후 모니터링 집계에서 제외 (Phase 6)

## 테스트 계획

### ProductionJobTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `actualQty_calculatedByCeil` | `ceil(부족분 / (수율 × 0.9))` 공식 검증 |
| `totalTime_calculatedCorrectly` | `평균 생산시간 × 실 생산량` 검증 |

### ProductionQueueTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `enqueue_firstJob_isCurrentJob` | 첫 등록 작업이 `peek()`으로 조회됨 |
| `enqueue_multipleJobs_fifoOrder` | 두 번째 작업은 `getWaiting()`에 포함됨 |

### ApprovalControllerTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `approve_withSufficientStock_confirmsOrder` | 재고 충분 시 `CONFIRMED` 전환, 재고 차감 |
| `approve_withInsufficientStock_producesOrder` | 재고 부족 시 `PRODUCING` 전환, 생산 큐 등록 |
| `reject_order_rejectsOrder` | 거절 시 `REJECTED` 전환 |
| `run_withNoReservedOrders_showsEmptyMessage` | RESERVED 없을 때 안내 메시지 출력 |

### ApprovalOutputViewTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `showReservedList_containsOrderIds` | 목록에 주문번호 포함 |
| `showConfirmed_containsStatusConfirmed` | 출력에 CONFIRMED 포함 |
| `showProducing_containsActualQtyAndTotalTime` | 출력에 실 생산량·총 생산시간 포함 |

## MainController 수정 내용

```java
// 기존
case "3" -> out.showNotImplemented();

// 변경
case "3" -> approvalController.run();
```

`ApprovalController`는 `MainController` 생성자에서 조립한다.

## 완료 기준

- `[3] 주문 승인/거절` 진입 후 RESERVED 목록 조회 및 처리가 동작한다
- 재고 충분 시 `CONFIRMED` 전환 및 재고 차감
- 재고 부족 시 `PRODUCING` 전환 및 생산 큐 등록
- 거절 시 즉시 `REJECTED` 전환
- `./gradlew test` 전체 통과

---

## TDD 사이클 계획

> 각 사이클은 하나의 동작만 검증한다. 승인 후 테스트 작성 → RED 확인 → GREEN 구현 → REVIEW 순으로 진행한다.

### 사이클 1 — ProductionJob: 실 생산량 계산

- **테스트 이름**: `actualQty_calculatedByCeil`
- **@DisplayName**: `실 생산량은 ceil(부족분 / (수율 × 0.9)) 공식으로 계산된다`
- **Given**: 부족분 170, 수율 0.9, 평균 생산시간 0.5
- **When**: `ProductionJob` 생성
- **Then**: `actualQty` == `ceil(170 / (0.9 × 0.9))` == 210

### 사이클 2 — ProductionJob: 총 생산시간 계산

- **테스트 이름**: `totalTime_calculatedCorrectly`
- **@DisplayName**: `총 생산시간은 평균 생산시간 × 실 생산량으로 계산된다`
- **Given**: 사이클 1과 동일
- **When**: `ProductionJob` 생성
- **Then**: `totalTime` == `0.5 × 210` == 105.0

### 사이클 3 — ProductionQueue: 첫 등록 작업이 현재 작업으로 조회됨

- **테스트 이름**: `enqueue_firstJob_isCurrentJob`
- **@DisplayName**: `첫 번째 등록 작업이 peek()으로 조회된다`
- **Given**: 빈 `ProductionQueue`
- **When**: `ProductionJob` 하나 enqueue 후 `peek()` 호출
- **Then**: 반환값이 등록한 작업과 동일

### 사이클 4 — ProductionQueue: 두 번째 작업은 대기 목록에 포함됨

- **테스트 이름**: `enqueue_secondJob_isInWaitingList`
- **@DisplayName**: `두 번째 등록 작업은 getWaiting()에 포함된다`
- **Given**: 작업 2개 등록
- **When**: `getWaiting()` 호출
- **Then**: 두 번째 작업이 포함, 첫 번째 작업은 미포함

### 사이클 5 — ApprovalController: 재고 충분 시 CONFIRMED 전환

- **테스트 이름**: `approve_withSufficientStock_confirmsOrder`
- **@DisplayName**: `재고가 충분할 때 승인하면 CONFIRMED로 전환되고 재고가 차감된다`
- **Given**: 재고 480인 시료, 수량 200 RESERVED 주문, Y(승인) 입력
- **When**: `approvalController.run()` 호출
- **Then**: 주문 상태 == `CONFIRMED`, 시료 재고 == 280

### 사이클 6 — ApprovalController: 재고 부족 시 PRODUCING 전환 및 생산 큐 등록

- **테스트 이름**: `approve_withInsufficientStock_producesOrder`
- **@DisplayName**: `재고가 부족할 때 승인하면 PRODUCING으로 전환되고 생산 큐에 등록된다`
- **Given**: 재고 30인 시료, 수량 200 RESERVED 주문, Y(승인) 입력
- **When**: `approvalController.run()` 호출
- **Then**: 주문 상태 == `PRODUCING`, 생산 큐 크기 == 1

### 사이클 7 — ApprovalController: 거절 시 REJECTED 전환

- **테스트 이름**: `reject_order_rejectsOrder`
- **@DisplayName**: `거절 입력 시 주문이 REJECTED로 전환된다`
- **Given**: RESERVED 주문, N(거절) 입력
- **When**: `approvalController.run()` 호출
- **Then**: 주문 상태 == `REJECTED`

### 사이클 8 — ApprovalController: RESERVED 없을 때 안내 메시지

- **테스트 이름**: `run_withNoReservedOrders_showsEmptyMessage`
- **@DisplayName**: `대기 주문이 없을 때 안내 메시지를 출력한다`
- **Given**: 빈 OrderRepository
- **When**: `approvalController.run()` 호출
- **Then**: 안내 메시지 출력, 추가 입력 없이 복귀
