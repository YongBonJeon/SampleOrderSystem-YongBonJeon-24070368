# 코드 리뷰 보고서

> 리뷰 일시: 2026-06-12  
> 리뷰 범위: Phase 1–7 전체 (`main` 브랜치 기준, 미커밋 변경 포함)  
> 방법: 7개 탐지 앵글 × 병렬 에이전트 → 38개 후보 → 검증 → 최종 8건

---

## 요약

| # | 위치 | 심각도 | 유형 | 상태 |
|---|------|--------|------|------|
| [F-1](#f-1-생산-큐-복원-정렬-오류--재시작-시-생산라인-영구-정지) | `ProductionQueueRestorer.java:20` | 🔴 Critical | 정렬 키 오류 → 재시작 후 생산라인 영구 정지 | ✅ 수정 완료 |
| [F-2](#f-2-producingrelease-경로에서-재고-차감-누락) | `ReleaseController.java` | 🔴 Critical | PRODUCING 경로 재고 차감 누락 | ✅ 수정 완료 |
| [F-3](#f-3-complete에서-actualqty총-투입량를-재고에-가산) | `ProductionLineController.java:63` | 🔴 Critical | `actualQty` vs `shortage` 혼동 → 재고 과대계상 | ⚠️ 수정 불필요 (검토 결과) |
| [F-4](#f-4-order-생성자-사이드이펙트) | `Order.java:22` | 🟡 Design | 생성자 사이드이펙트 → 저장소 구현 의존 | 미수정 |
| [F-5](#f-5-productionqueuerestorer가-enqueue-사이드이펙트를-사후-무효화) | `ProductionQueueRestorer.java:26` | 🟡 Design | `enqueue()` 사이드이펙트와 싸우는 복원 로직 | 미수정 |
| [F-6](#f-6-maincontroller-루프에서-findall-2회-중복-호출) | `MainController.java:43` | 🔵 Efficiency | `findAll()` 2회 중복 호출 | ✅ 수정 완료 |
| [F-7](#f-7-outputview-무인수-오버로드가-데드코드) | `OutputView.java:35` | 🔵 Cleanup | 0값 데드코드 오버로드 | 미수정 |
| [F-8](#f-8-databaseorderrepositoryfindbystatuspersistence가-전체-스캔) | `DatabaseOrderRepository.java:69` | 🔵 Efficiency | `findByStatus` 전체 테이블 스캔 | 미수정 |

Critical 3건(F-1, F-2, F-3) 중 F-1, F-2는 수정 완료. F-3은 F-2 수정(출고 시 `order.quantity` 차감)으로 재고 계산이 올바르게 정리되므로 `actualQty` 가산은 의도된 과잉생산 버퍼로 판단, 수정 불필요.

---

## Critical

### F-1: 생산 큐 복원 정렬 오류 → 재시작 시 생산라인 영구 정지

**파일**: `src/main/java/org/example/controller/ProductionQueueRestorer.java:20`

#### 문제 코드

```java
producing.stream()
    .sorted(Comparator.comparing(Order::getOrderedAt))  // ← 접수 시각으로 정렬
    .forEach(order -> ...);
```

#### 원인

`orderedAt`은 고객이 주문을 넣은 시각이다. 관리자가 주문을 승인하는 순서(큐 진입 순서)와 무관하다.

`ApprovalController`는 큐에 두 번째 이후로 들어간 job의 `startedAt`을 `null`로 저장한다.

```java
// ApprovalController.java
order.setStartedAt(job.getStartedAt()); // 2번째+ job은 null
orderRepository.update(order);
```

재시작 후 `orderedAt` 정렬이 승인 순서와 다를 경우, `startedAt=null`인 job이 큐의 head가 된다.

`ProductionLineController.run()`은 `startedAt`이 null이면 완료 검사를 건너뛴다.

```java
if (current.getStartedAt() != null) {   // null이면 완료 체크 전혀 없음
    ...
    if (elapsedMinutes >= current.getTotalTime()) { complete(); return; }
}
out.showProductionLine(...);  // 표시만 되고 영원히 완료되지 않음
```

#### 재현 시나리오

1. 고객 B가 09:00 주문, 고객 A가 10:00 주문 (같은 시료, 재고 부족)
2. 관리자가 **A를 먼저** 승인 → A가 큐 head, `order_A.startedAt = 11:00` 저장
3. B를 이후 승인 → B가 2번째, `order_B.startedAt = null` 저장
4. **앱 재시작**
5. `restore()`가 orderedAt 오름차순 정렬 → B(09:00) 먼저 enqueue
6. `enqueue(B)`: 큐 빈 상태 → `job_B.startedAt = now` 설정됨
7. `job_B.setStartedAt(order_B.getStartedAt())` → **null로 덮어씀**
8. 큐 head = B, `startedAt = null` → 완료 검사 영구 스킵 → **생산라인 정지**

#### 수정 방향

정렬 기준을 변경한다. `startedAt`이 non-null인 주문(실제 head였던 주문)을 반드시 앞에 두어야 한다.

```java
.sorted(Comparator.comparing(
    order -> order.getStartedAt() != null ? order.getStartedAt() : LocalDateTime.MAX
))
```

---

### F-2: PRODUCING→RELEASE 경로에서 재고 차감 누락

**파일**: `src/main/java/org/example/controller/ReleaseController.java`

#### 문제

직접 CONFIRMED 경로(재고 충분)는 승인 시 즉시 재고를 차감한다.

```java
// ApprovalController.java — CONFIRMED 경로
sample.setStock(sample.getStock() - order.getQuantity());  // 재고 차감
sampleRepository.update(sample);
```

반면 PRODUCING 경로는:

| 단계 | 재고 변화 |
|------|-----------|
| 승인 시 (`PRODUCING`) | 변동 없음 |
| 생산 완료 (`complete()`) | `+= actualQty` (증가) |
| 출고 (`RELEASE`) | **변동 없음** ← 문제 |

`ReleaseController`는 `SampleRepository`에 대한 의존성 자체가 없다.

#### 결과

재고 30개, 주문 100개 → 생산 완료 후 재고 117개 → 출고 후 **여전히 117개**.
PRODUCING 사이클이 반복될수록 재고 수치가 실물과 계속 벌어진다.

#### 수정 방향

`ReleaseController`에 `SampleRepository`를 주입하고, RELEASE 전환 시 주문 수량만큼 차감한다.

```java
// ReleaseController에 추가
order.setStatus(OrderStatus.RELEASE);
orderRepository.update(order);
sampleRepository.findById(order.getSampleId()).ifPresent(sample -> {
    sample.setStock(sample.getStock() - order.getQuantity());
    sampleRepository.update(sample);
});
```

---

### F-3: `complete()`에서 `actualQty`(총 투입량)를 재고에 가산

**파일**: `src/main/java/org/example/controller/ProductionLineController.java:63`

#### 문제 코드

```java
int newStock = sample.getStock() + job.getActualQty();  // actualQty = 총 투입량
```

#### 원인

`ProductionJob`의 수량 관계:

```
shortage  = order.quantity - existing_stock  (순 부족량, 고객에게 필요한 수)
actualQty = ceil(shortage / (yieldRate × 0.9))  (수율 손실을 감안한 총 투입량)
```

예: shortage=70, yieldRate=0.9 → `actualQty = ceil(70/0.81) = 87`

생산 완료 후 재고에 추가되어야 하는 양은 **생산된 완제품 수(≈shortage)** 이지, 투입한 원재료 수(actualQty)가 아니다. 현재 코드는 매 생산 완료마다 `(actualQty - shortage)` 단위의 유령 재고를 생성한다.

#### 수치 예시

| 항목 | 값 |
|------|-----|
| 초기 재고 | 30 |
| 주문 수량 | 100 |
| shortage | 70 |
| actualQty (투입) | 87 |
| 생산 완료 후 재고 (현재) | 30 + 87 = **117** |
| 생산 완료 후 재고 (올바름) | 30 + 70 = **100** |
| 과대계상 | +17 (매 PRODUCING 사이클마다 누적) |

#### 수정 방향

`job.getShortage()`를 가산하거나, 복원용 생성자를 사용할 경우 별도로 저장된 순 필요량을 사용한다.

```java
int newStock = sample.getStock() + job.getShortage();
```

> **참고**: `ProductionJob`의 복원용 4인수 생성자(`shortage=0`으로 고정)를 사용하는 경우, `shortage`가 0이 되어 위 수정이 동작하지 않는다. 복원 시에도 `shortage`를 보존하려면 `Order`에 `shortage` 필드를 추가하거나 `actualQty`로부터 역산해야 한다.

---

## Design

### F-4: `Order` 생성자 사이드이펙트

**파일**: `src/main/java/org/example/model/Order.java:22`

#### 문제 코드

```java
public Order(String orderId, ...) {
    ...
    this.orderedAt = LocalDateTime.now();  // 항상 현재 시각
}
```

DB/File/JSON에서 객체를 재구성할 때도 이 생성자를 거치므로, 각 저장소 구현체는 반드시 즉시 `setOrderedAt()`으로 덮어써야 한다. 이번 diff에서 `DatabaseOrderRepository.map()`은 수정됐지만, 이 의무는 타입 시스템으로 강제되지 않는다.

미래에 새 저장소 구현체를 추가할 때 setter 호출을 누락하면 조용히 틀린 시각이 기록된다.

#### 수정 방향

저장소 재구성 전용 경로를 분리한다.

```java
// 신규 주문 생성용 (기존 생성자 유지)
public Order(String orderId, String sampleId, String customerName, int quantity, OrderStatus status) { ... }

// 저장소 재구성용
public static Order reconstruct(String orderId, String sampleId, String customerName,
                                 int quantity, OrderStatus status, LocalDateTime orderedAt) {
    Order o = new Order(orderId, sampleId, customerName, quantity, status);
    o.orderedAt = orderedAt;  // 직접 설정 (setter 불필요)
    return o;
}
```

---

### F-5: `ProductionQueueRestorer`가 `enqueue()` 사이드이펙트를 사후 무효화

**파일**: `src/main/java/org/example/controller/ProductionQueueRestorer.java:26`

#### 문제 코드

```java
queue.enqueue(job);                      // enqueue가 head job에 startedAt 설정
job.setStartedAt(order.getStartedAt()); // 바로 덮어씀
```

`ProductionQueue.enqueue()`는 큐가 비어 있을 때 첫 job에 `startedAt`을 자동 설정한다는 규칙을 갖는다. `ProductionQueueRestorer`는 이 동작을 원치 않아서 사후에 덮어쓴다. 두 클래스의 계약이 충돌하는 구조다.

#### 수정 방향

`ProductionQueue`에 복원 전용 메서드를 추가해 계약을 명시한다.

```java
// ProductionQueue에 추가
public void enqueueRestored(ProductionJob job) {
    queue.addLast(job);  // startedAt 건드리지 않음
}
```

---

## Efficiency / Cleanup

### F-6: `MainController` 루프에서 `findAll()` 2회 중복 호출

**파일**: `src/main/java/org/example/controller/MainController.java:43`

#### 문제 코드

```java
int sampleCount = sampleRepository.findAll().size();                            // 1회
int totalStock  = sampleRepository.findAll().stream().mapToInt(...).sum();      // 2회 (중복)
```

FILE/JSON 모드에서는 메인 메뉴가 표시될 때마다 같은 파일이 2회 역직렬화된다.

#### 수정

```java
List<Sample> samples = sampleRepository.findAll();
int sampleCount = samples.size();
int totalStock  = samples.stream().mapToInt(Sample::getStock).sum();
```

---

### F-7: `OutputView` 무인수 오버로드가 데드코드

**파일**: `src/main/java/org/example/view/OutputView.java:35`

#### 문제 코드

```java
public void showMainMenu() {
    showMainMenu(0, 0, 0, 0, LocalDateTime.now());  // 항상 모든 수치가 0
}
```

프로덕션 코드에서 이 오버로드를 호출하는 곳이 없다. 테스트에서도 사용하지 않는다.

실수로 호출될 경우 "등록 시료 0종 / 총 재고 0 ea / 전체 주문 0건"을 아무 경고 없이 표시한다.

#### 수정

해당 메서드를 삭제한다.

---

### F-8: `DatabaseOrderRepository.findByStatus()` 전체 테이블 스캔

**파일**: `src/main/java/org/example/repository/impl/DatabaseOrderRepository.java:69`

#### 문제 코드

```java
public List<Order> findByStatus(OrderStatus status) {
    return findAll().stream()
            .filter(o -> o.getStatus() == status)
            .collect(Collectors.toList());
}
```

`findAll()` (전체 스캔) + 인메모리 필터. `findByStatus`는 메인 루프, 모니터링, 큐 복원에서 반복 호출된다.

#### 호출 빈도 (메인 루프 한 번 기준)

| 호출 위치 | 횟수 |
|-----------|------|
| `MainController.run()` (PRODUCING 집계) | 1회 |
| `MonitoringController.showOrderCounts()` | 4회 (상태별) |
| `MonitoringController.showStockStatus()` | 2회 (RESERVED, PRODUCING) |
| 합계 | 7회 (모니터링 진입 시) |

#### 수정

```java
public List<Order> findByStatus(OrderStatus status) {
    String sql = "SELECT * FROM orders WHERE status = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, status.name());
        ResultSet rs = ps.executeQuery();
        List<Order> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    } catch (SQLException e) {
        throw new RuntimeException("주문 상태별 조회 실패", e);
    }
}
```

---

## 참고: 이번 diff에서 올바르게 수정된 항목

| 항목 | 내용 |
|------|------|
| `DatabaseOrderRepository.map()` | `ordered_at` 컬럼 복원 누락 수정 (`setOrderedAt` 추가) |
| `Order.java` | `setOrderedAt()` setter 추가 |
| `DatabaseOrderRepositoryTest` | `orderedAt` 복원 검증 테스트 추가 |

---

## 관련 문서

- [docs/PLAN.md](PLAN.md) — Phase별 진행 현황
- [docs/PRD.md](PRD.md) — 기능 명세 (재고 규칙, 잔여율 정의 포함)
- [docs/design/phase5.md](design/phase5.md) — 생산라인 설계
- [docs/design/phase7.md](design/phase7.md) — 영속성 설계
