# Phase 1 — 프로젝트 기반 구조

## 목표

이후 모든 Phase가 올라갈 MVC 골격을 완성한다.  
기능 구현은 없고, 구조·인터페이스·진입점만 확정한다.

## 생성 파일 목록

```
build.gradle                                         ← 의존성 추가
src/main/java/org/example/
├── Application.java                                 ← 진입점
├── controller/
│   └── MainController.java                          ← 메뉴 루프
├── view/
│   ├── InputView.java                               ← 콘솔 입력
│   └── OutputView.java                              ← 콘솔 출력
├── model/
│   └── (Phase 2~부터 도메인 추가)
└── repository/
    ├── SampleRepository.java                        ← 인터페이스
    ├── OrderRepository.java                         ← 인터페이스
    └── impl/
        ├── InMemorySampleRepository.java            ← 빈 구현체
        └── InMemoryOrderRepository.java             ← 빈 구현체
```

## 의존성 (`build.gradle`)

```groovy
dependencies {
    implementation 'com.h2database:h2:2.2.224'
    implementation 'com.google.code.gson:gson:2.10.1'

    testImplementation platform('org.junit:junit-bom:6.0.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

application {
    mainClass = 'org.example.Application'
}
```

## 클래스 설계

### Application.java

```java
public class Application {
    public static void main(String[] args) {
        SampleRepository sampleRepo = new InMemorySampleRepository();
        OrderRepository  orderRepo  = new InMemoryOrderRepository();
        InputView  in  = new InputView();
        OutputView out = new OutputView();

        new MainController(sampleRepo, orderRepo, in, out).run();
    }
}
```

### MainController.java

```java
public class MainController {
    // 생성자 주입: SampleRepository, OrderRepository, InputView, OutputView

    public void run() {
        while (true) {
            out.showMainMenu();           // 메뉴 출력 + 시스템 현황 요약
            String input = in.readLine();
            switch (input) {
                case "1" -> sampleManagement();
                case "2" -> orderSample();
                case "3" -> approveOrReject();
                case "4" -> monitoring();
                case "5" -> productionLine();
                case "6" -> release();
                case "0" -> { out.println("종료합니다."); return; }
                default  -> out.showError("올바른 번호를 입력하세요.");
            }
        }
    }

    // 각 메뉴 메서드는 Phase 1에서 "미구현" 메시지 출력
    private void sampleManagement() { out.showNotImplemented(); }
    private void orderSample()      { out.showNotImplemented(); }
    private void approveOrReject()  { out.showNotImplemented(); }
    private void monitoring()       { out.showNotImplemented(); }
    private void productionLine()   { out.showNotImplemented(); }
    private void release()          { out.showNotImplemented(); }
}
```

### InputView.java

```java
public class InputView {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine()              // 한 줄 입력
    public int    readInt()               // 정수 입력 (잘못된 입력 시 -1 반환)
    public String readLine(String prompt) // 프롬프트 출력 후 입력
}
```

### OutputView.java

```java
public class OutputView {
    public void showMainMenu()          // 배너 + 시스템 현황 + 메뉴 목록
    public void showNotImplemented()    // "[ 미구현 ] 준비 중입니다."
    public void showError(String msg)   // "[ERROR] " + msg
    public void println(String msg)
    public void printDivider()          // "=" * 60
}
```

### SampleRepository.java (인터페이스)

```java
public interface SampleRepository {
    Sample          save(Sample sample);
    Optional<Sample> findById(String id);
    List<Sample>    findAll();
    Sample          update(Sample sample);
    void            deleteById(String id);
}
```

### OrderRepository.java (인터페이스)

```java
public interface OrderRepository {
    Order           save(Order order);
    Optional<Order> findById(String orderId);
    List<Order>     findAll();
    List<Order>     findByStatus(OrderStatus status);
    Order           update(Order order);
}
```

### InMemorySampleRepository.java / InMemoryOrderRepository.java

- `Map<String, T>`로 데이터 보관
- 인터페이스 메서드를 모두 구현하되, Phase 1에서는 빈 컬렉션 반환으로만 완성

## 메인 메뉴 출력 형식 (예시)

```
============================================================
  반도체 시료 생산주문관리 시스템
============================================================
  시스템 현황 | 등록 시료: 0종  총 재고: 0 ea  전체 주문: 0건

  [1] 시료 관리       [2] 시료 주문
  [3] 주문 승인/거절  [4] 모니터링
  [5] 생산라인 조회   [6] 출고 처리
  [0] 종료
------------------------------------------------------------
선택 >
```

## 테스트 계획

| 테스트 클래스 | 검증 내용 |
|---------------|-----------|
| `MainControllerTest` | "0" 입력 시 루프 종료, 잘못된 입력 시 오류 메시지 출력 |
| `InputViewTest` | 숫자 아닌 입력 시 `readInt()` → -1 반환 |
| `InMemorySampleRepositoryTest` | save / findById / findAll / deleteById 동작 |
| `InMemoryOrderRepositoryTest` | save / findByStatus 동작 |

## 완료 기준

- `./gradlew run` 시 메인 메뉴가 출력된다.
- 1~6 입력 시 "미구현" 메시지, 0 입력 시 종료된다.
- `./gradlew test` 통과.
