#tns Phase 3 — 주문(Order) 접수

## 목표

메뉴 `[2] 시료 주문` 기능을 완성한다.  
시료 ID / 고객명 / 수량을 입력받아 확인 절차를 거쳐 `RESERVED` 상태의 주문을 생성한다.

## 사전 확인 (이미 구현됨)

| 항목 | 파일 | 비고 |
|------|------|------|
| `OrderStatus` enum | `model/OrderStatus.java` | 5개 상태 정의 완료 |
| `Order` 도메인 모델 | `model/Order.java` | 필드 및 생성자 완료 |
| `OrderRepository` 인터페이스 | `repository/OrderRepository.java` | CRUD + `findByStatus` 완료 |
| `InMemoryOrderRepository` | `repository/impl/InMemoryOrderRepository.java` | 완료 |

Phase 3에서 **신규 작성**할 대상: `OrderIdGenerator`, `OrderController`, `OrderInputView`, `OrderOutputView`, `MainController` 수정.

## 변경 파일 목록

```
src/main/java/org/example/
├── util/
│   └── OrderIdGenerator.java          ← 신규: 주문번호 생성 유틸
├── controller/
│   ├── OrderController.java           ← 신규: 주문 접수 흐름
│   └── MainController.java            ← 수정: case "2" → OrderController 위임
└── view/
    ├── OrderInputView.java            ← 신규: 주문 입력
    └── OrderOutputView.java           ← 신규: 주문 출력

src/test/java/org/example/
├── util/
│   └── OrderIdGeneratorTest.java      ← 신규
├── controller/
│   └── OrderControllerTest.java       ← 신규
└── view/
    └── OrderOutputViewTest.java       ← 신규
```

## 클래스 설계

### OrderIdGenerator.java

```java
public class OrderIdGenerator {
    // 날짜별 시퀀스를 내부 Map으로 관리
    // 같은 날짜에 여러 주문 생성 시 NNNN 순번 증가

    public OrderIdGenerator(LocalDate date, List<String> existingOrderIds)  // 기존 주문 ID로 시퀀스 복원
    public OrderIdGenerator(LocalDate date)  // 테스트용 날짜 주입 (기존 주문 없음)
    public OrderIdGenerator()                // 프로덕션용 — LocalDate.now() 사용
    public String generate()                 // "ORD-YYYYMMDD-NNNN" 형식 반환
}
```

- `NNNN`은 1부터 시작하는 4자리 0-패딩 숫자 (예: `0001`)
- 날짜가 바뀌면 순번을 1로 리셋 (날짜별 독립 Map 키)
- 재시작 시 기존 주문 ID에서 오늘 날짜 최대 시퀀스를 읽어 이어받음 (ID 중복 방지)
- 생성자 오버로드로 테스트에서 날짜 고정 가능

### OrderController.java

```java
public class OrderController {
    // 생성자 주입: SampleRepository, OrderRepository,
    //              OrderIdGenerator, OrderInputView, OrderOutputView

    public void run() {
        placeOrder();
    }

    private void placeOrder()  // 주문 접수 흐름
}
```

**`placeOrder()` 흐름**:

1. 시료 ID 입력 → `SampleRepository.findById()` 조회
   - 없으면 오류 메시지 출력 후 복귀
2. 고객명 입력
3. 주문 수량 입력 (1 이상 정수)
4. 입력 확인 화면 출력
5. `Y` → `OrderIdGenerator.generate()`로 주문번호 생성, `RESERVED` 상태로 저장, 접수 완료 출력
6. `N` → "주문이 취소되었습니다." 출력 후 복귀

### OrderInputView.java

```java
public class OrderInputView {
    // 생성자: Scanner 주입 (MainController와 동일한 Scanner 공유)

    public String readSampleId()      // 시료 ID 입력
    public String readCustomerName()  // 고객명 입력
    public int readQuantity()         // 주문 수량 입력 (1 이상 정수 검증)
    public String readConfirm()       // Y/N 확인
}
```

### OrderOutputView.java

```java
public class OrderOutputView {
    // 생성자: PrintStream 주입

    public void showOrderConfirm(Sample sample, String customerName, int quantity)
    // 입력 확인 화면 출력

    public void showOrderComplete(Order order)
    // 접수 완료 출력 (주문번호, 상태)

    public void showOrderCancelled()
    // 주문 취소 메시지

    public void showSampleNotFound(String sampleId)
    // 등록되지 않은 시료 ID 오류

    public void showError(String msg)
}
```

## 화면 출력 형식

### 입력 흐름

```
=== 시료 주문 ===
시료 ID    > S-001
고객명     > 한국반도체연구소
주문 수량  > 200

[주문 확인]
  시료     : S-001 실리콘 웨이퍼-8인치
  고객명   : 한국반도체연구소
  주문 수량: 200 ea
주문하시겠습니까? (Y/N) > Y

주문 접수 완료
  주문번호 : ORD-20260612-0001
  상태     : RESERVED
```

### 취소 흐름

```
주문하시겠습니까? (Y/N) > N
주문이 취소되었습니다.
```

### 오류 흐름

```
시료 ID    > S-999
등록되지 않은 시료 ID입니다: S-999
```

## 비즈니스 규칙

- 등록된 시료 ID만 주문 가능 (`SampleRepository.findById()`로 검증)
- 주문 수량은 1 이상 정수만 허용 (0 이하 입력 시 재입력 요청)
- 주문 생성 시 상태는 반드시 `RESERVED`
- 주문번호 형식: `ORD-YYYYMMDD-NNNN` (날짜별 순번 관리)
- `N` 입력 시 저장하지 않고 복귀

## 테스트 계획

### OrderIdGeneratorTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `generate_firstOrder_returnsSeq0001` | 첫 번째 주문번호가 `ORD-YYYYMMDD-0001` 형식 |
| `generate_secondOrder_returnsSeq0002` | 두 번째 호출 시 순번 증가 |
| `generate_newDate_resetsSequence` | 날짜 변경 시 순번이 0001로 리셋 |

### OrderControllerTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `placeOrder_withValidInput_savesReservedOrder` | 유효한 입력 후 Y → `RESERVED` 주문 저장 |
| `placeOrder_withUnknownSampleId_showsError` | 없는 시료 ID → 오류 메시지, 저장 안 됨 |
| `placeOrder_withCancelConfirm_doesNotSave` | N 입력 → 저장 안 됨, 취소 메시지 출력 |
| `placeOrder_assignsCorrectOrderId` | 저장된 주문의 주문번호가 `ORD-` 형식을 따름 |

### OrderOutputViewTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `showOrderConfirm_containsSampleAndQuantity` | 확인 화면에 시료명·고객명·수량 포함 |
| `showOrderComplete_containsOrderIdAndStatus` | 완료 메시지에 주문번호·RESERVED 포함 |
| `showSampleNotFound_containsSampleId` | 오류 메시지에 입력한 시료 ID 포함 |

## MainController 수정 내용

```java
// 기존
case "2" -> out.showNotImplemented();

// 변경
case "2" -> orderController.run();
```

`OrderController`는 `MainController` 생성자에서 조립한다.

## 완료 기준

- `[2] 시료 주문` 진입 후 시료 ID / 고객명 / 수량 입력 → 확인 → `RESERVED` 주문 저장
- 등록되지 않은 시료 ID 입력 시 오류 메시지 출력, 저장 안 됨
- `N` 입력 시 주문 저장 없이 복귀
- `./gradlew test` 전체 통과

---

## TDD 사이클 계획

> 각 사이클은 하나의 동작만 검증한다. 승인 후 테스트 작성 → RED 확인 → GREEN 구현 → REVIEW 순으로 진행한다.

### 사이클 1 — OrderIdGenerator: 첫 호출 시 형식 반환

- **테스트 이름**: `generate_firstCall_returnsFormattedId`
- **@DisplayName**: `첫 번째 호출 시 ORD-날짜-0001 형식을 반환한다`
- **Given**: 날짜 `2026-06-12`를 주입한 `OrderIdGenerator`
- **When**: `generate()` 한 번 호출
- **Then**: 반환값 == `"ORD-20260612-0001"`

### 사이클 2 — OrderIdGenerator: 두 번째 호출 시 순번 증가

- **테스트 이름**: `generate_secondCall_incrementsSequence`
- **@DisplayName**: `같은 날 두 번째 호출 시 순번이 0002로 증가한다`
- **Given**: 동일 날짜의 `OrderIdGenerator`
- **When**: `generate()` 두 번 호출
- **Then**: 두 번째 반환값 == `"ORD-20260612-0002"`

### 사이클 3 — OrderIdGenerator: 날짜 변경 시 순번 리셋

- **테스트 이름**: `generate_newDate_resetsSequence`
- **@DisplayName**: `날짜가 바뀌면 순번이 0001로 리셋된다`
- **Given**: 날짜 A에서 한 번 호출한 `OrderIdGenerator`
- **When**: 날짜 B로 바꾼 후 `generate()` 호출
- **Then**: 반환값의 날짜 부분이 B, 순번 == `0001`

### 사이클 4 — OrderController: 유효한 입력으로 RESERVED 주문 저장

- **테스트 이름**: `placeOrder_withValidInput_savesReservedOrder`
- **@DisplayName**: `유효한 입력 후 Y 확인 시 RESERVED 주문이 저장된다`
- **Given**: 등록된 시료, Y 확인 입력
- **When**: `orderController.run()` 호출
- **Then**: `OrderRepository`에 `RESERVED` 상태 주문 1건 저장

### 사이클 5 — OrderController: 미등록 시료 ID 오류 처리

- **테스트 이름**: `placeOrder_withUnknownSampleId_showsError`
- **@DisplayName**: `등록되지 않은 시료 ID 입력 시 오류 메시지를 출력하고 저장하지 않는다`
- **Given**: 없는 시료 ID 입력
- **When**: `orderController.run()` 호출
- **Then**: 오류 메시지 출력, 저장 0건

### 사이클 6 — OrderController: N 입력 시 저장 없이 취소

- **테스트 이름**: `placeOrder_withCancelConfirm_doesNotSave`
- **@DisplayName**: `N 확인 입력 시 주문이 저장되지 않고 취소 메시지가 출력된다`
- **Given**: 유효한 입력, N 확인 입력
- **When**: `orderController.run()` 호출
- **Then**: 저장 0건, 취소 메시지 출력

### 사이클 7 — OrderOutputView: 확인 화면 출력

- **테스트 이름**: `showOrderConfirm_containsSampleAndQuantity`
- **@DisplayName**: `주문 확인 화면에 시료명·고객명·수량이 포함된다`

### 사이클 8 — OrderOutputView: 접수 완료 출력

- **테스트 이름**: `showOrderComplete_containsOrderIdAndStatus`
- **@DisplayName**: `접수 완료 메시지에 주문번호와 RESERVED 상태가 포함된다`
