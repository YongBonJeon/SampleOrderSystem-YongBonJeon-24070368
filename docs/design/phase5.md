# Phase 5 — 생산 라인 (시간 기반)

## 목표

메뉴 `[5] 생산라인 조회` 기능을 완성하고, **시간 경과에 따른 자동 완료 처리**를 구현한다.
조회 시점에 경과 시간이 총 생산 시간을 넘으면 사람의 입력 없이 자동으로 재고를 증가시키고 주문을 `CONFIRMED`로 전환한다.

## 사전 확인 (이미 구현됨)

| 항목 | 파일 | 비고 |
|------|------|------|
| `ProductionJob` | `model/ProductionJob.java` | actualQty · totalTime 계산 완료 |
| `ProductionQueue` | `queue/ProductionQueue.java` | enqueue / peek / getWaiting / dequeue 완료 |
| `Sample.setStock()` | `model/Sample.java` | 재고 증가에 재사용 |
| `OrderRepository.update()` | `repository/OrderRepository.java` | 상태 전환에 재사용 |
| `SampleRepository.update()` | `repository/SampleRepository.java` | 재고 반영에 재사용 |

## 변경 파일 목록

```
src/main/java/org/example/
├── model/
│   └── ProductionJob.java             ← 수정: avgProductionTime, startedAt 추가
├── queue/
│   └── ProductionQueue.java           ← 수정: Clock 주입, startedAt 자동 기록
├── controller/
│   ├── ProductionLineController.java  ← 수정: InputView 제거, 시간 기반 자동 완료
│   └── MainController.java            (변경 없음)
└── view/
    └── ProductionLineOutputView.java  ← 수정: 진행률 · 완료 예정 시각 출력

src/test/java/org/example/
├── model/
│   └── ProductionJobTest.java         ← 수정: avgProductionTime 테스트 추가
├── queue/
│   └── ProductionQueueTest.java       ← 수정: startedAt 설정 테스트 추가
├── controller/
│   └── ProductionLineControllerTest.java ← 전면 재작성: 시간 기반 테스트
└── view/
    └── ProductionLineOutputViewTest.java ← 수정: showProductionLine 시그니처 변경 반영
```

## 클래스 설계

### ProductionJob.java — avgProductionTime · startedAt 추가

```java
private final double avgProductionTime;  // 단위 생산 시간 (min/ea)
private LocalDateTime startedAt;         // 큐 선두 진입 시각 (ProductionQueue가 설정)

// 추가 getter / setter
public double getAvgProductionTime()
public LocalDateTime getStartedAt()
public void setStartedAt(LocalDateTime startedAt)
```

### ProductionQueue.java — Clock 주입 + startedAt 자동 기록

```java
// 생성자
public ProductionQueue()                            // LocalDateTime::now 사용
public ProductionQueue(Supplier<LocalDateTime> clock)

// enqueue: 큐가 비어있었으면 첫 번째 작업에 startedAt 기록
public void enqueue(ProductionJob job) {
    boolean wasEmpty = queue.isEmpty();
    queue.addLast(job);
    if (wasEmpty) job.setStartedAt(clock.get());
}

// dequeue: 다음 대기 작업이 있으면 그 작업에 startedAt 기록
public ProductionJob dequeue() {
    ProductionJob removed = queue.removeFirst();
    if (!queue.isEmpty()) queue.peekFirst().setStartedAt(clock.get());
    return removed;
}
```

### ProductionLineController.java — InputView 제거, 시간 기반 자동 완료

```java
public class ProductionLineController {
    // 생성자 주입: SampleRepository, OrderRepository,
    //              ProductionQueue, ProductionLineOutputView
    //              Supplier<LocalDateTime> clock  (테스트용 오버로드)

    public void run() {
        // 1. 큐 비어있으면 안내 메시지 후 복귀
        // 2. 현재 작업의 경과 시간 계산
        // 3. 경과 >= totalTime → complete() (자동)
        // 4. 경과 < totalTime → showProductionLine(current, waiting, now)
    }

    private void complete()
    // dequeue() → 재고 증가 → CONFIRMED 전환 → showCompleted()
}
```

**`run()` 흐름:**

1. 큐가 비어있으면 `showNoJobInProgress()` 출력 후 복귀
2. 현재 작업의 `startedAt`을 기준으로 `elapsedMinutes = now - startedAt` 계산
3. `elapsedMinutes >= totalTime` → `complete()` 호출 후 복귀
4. 그 외 → `showProductionLine(current, waiting, now)` 출력 후 복귀

> `InputView`는 더 이상 필요 없다. 메뉴 선택 자체가 "조회" 행위이며, 완료 처리는 시간 조건이 충족될 때 자동으로 이루어진다.

### ProductionLineOutputView.java — 진행률 · 완료 예정 시각 추가

```java
// 시그니처 변경
public void showProductionLine(ProductionJob current, List<ProductionJob> waiting, LocalDateTime now)

// showCompleteConfirm() 제거 (더 이상 불필요)

// 표시 내용 (now 기준 계산)
// - elapsedMinutes = Duration.between(startedAt, now).toSeconds() / 60.0
// - progressPct = min(100.0, elapsedMinutes / totalTime * 100)
// - producedQty = min(actualQty, floor(elapsedMinutes / avgProductionTime))
// - estimatedCompletion = startedAt + totalTime (min → seconds 변환)
// - 대기 작업별 예상 완료 시각 (앞 작업 완료 시각에서 연쇄 계산)
```

## 화면 출력 형식

### 생산라인 조회 (생산 중)

```
=== 생산라인 조회 ===
현재 상태: RUNNING

[현재 생산 중]
  주문번호    : ORD-20260416-0038
  시료 ID     : SiC-파워기판-6인치
  실 생산량   : 61 ea (0.80 min/ea)
  진행        : ████████░░ 72%   완료 예정 09:49
  생산완료량  : 44 / 61 ea

[대기 큐] (2건)
  1. ORD-20260416-0040  S-002  실 생산량: 190 ea  예상 완료: 11:43
  2. ORD-20260416-0043  S-001  실 생산량: 206 ea  예상 완료: 14:28
```

### 생산 자동 완료 시

```
완료(자동): ORD-20260416-0038 → CONFIRMED / SiC-파워기판-6인치 재고 +61 ea
```

### 큐 비어있을 때

```
=== 생산라인 조회 ===
생산 중인 작업이 없습니다.
```

## 비즈니스 규칙

- 생산 완료 시 재고 증가량은 `actualQty` (실 생산량)
- 완료 처리 후 주문 상태: `PRODUCING → CONFIRMED`
- 완료는 사람의 입력 없이 **시간 조건** 충족 시 자동 처리
- `dequeue()` 후 다음 작업이 있으면 즉시 `startedAt` 기록 (FIFO 연속 생산)

## 테스트 계획

### ProductionJobTest (추가)

| 테스트 | 검증 내용 |
|--------|-----------|
| `avgProductionTime_storedCorrectly` | 생성자로 전달한 avgProductionTime이 getter로 조회된다 |
| `startedAt_defaultIsNull` | 생성 직후 startedAt이 null이다 |
| `setStartedAt_updatesField` | setStartedAt 호출 후 getStartedAt이 동일 값을 반환한다 |

### ProductionQueueTest (추가)

| 테스트 | 검증 내용 |
|--------|-----------|
| `enqueue_firstJob_setsStartedAt` | 첫 번째 enqueue 시 startedAt이 clock.get()으로 설정된다 |
| `enqueue_secondJob_doesNotSetStartedAt` | 두 번째 enqueue 시 두 번째 작업의 startedAt은 null |
| `dequeue_setsStartedAtOnNextJob` | dequeue() 후 새 선두 작업에 startedAt이 설정된다 |

### ProductionLineControllerTest (전면 재작성)

| 테스트 | 검증 내용 |
|--------|-----------|
| `run_withEmptyQueue_showsNoJobMessage` | 큐 비어있을 때 안내 메시지 출력 |
| `run_whenTimeElapsed_autoCompletes` | 경과 시간 >= totalTime → 자동으로 재고 증가 + CONFIRMED |
| `run_whenJobInProgress_doesNotComplete` | 경과 시간 < totalTime → 상태 변경 없음 |

### ProductionLineOutputViewTest (수정)

| 테스트 | 검증 내용 |
|--------|-----------|
| `showProductionLine_containsCurrentJobInfo` | 주문번호·실 생산량·진행률(%) 포함 |
| `showCompleted_containsOrderIdAndStock` | 완료 메시지에 주문번호·재고 증가량 포함 |

## MainController / Application 수정 내용

`ProductionLineController` 생성자에서 `InputView` 제거:

```java
// Application.java — 변경 전
new ProductionLineController(sampleRepo, orderRepo, productionQueue, inputView, outputView)

// Application.java — 변경 후
new ProductionLineController(sampleRepo, orderRepo, productionQueue, outputView)
```

## 완료 기준

- `[5] 생산라인 조회` 진입 시 현재 작업의 진행률과 완료 예정 시각이 표시된다
- 총 생산 시간이 경과하면 사람의 입력 없이 자동 완료 처리된다
- 큐가 비어있을 때 안내 메시지 출력 후 복귀한다
- `./gradlew test` 전체 통과
