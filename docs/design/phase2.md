# Phase 2 — 시료(Sample) 관리

## 목표

메뉴 `[1] 시료 관리`의 전체 기능을 완성한다.  
시료 등록 / 목록 조회 / 이름 검색 — 이후 모든 Phase의 기반 데이터가 된다.

## 변경 파일 목록

```
src/main/java/org/example/
├── controller/
│   └── SampleController.java          ← 신규: 시료 관리 서브 메뉴 흐름
├── view/
│   ├── SampleInputView.java           ← 신규: 시료 관련 입력
│   └── SampleOutputView.java          ← 신규: 시료 관련 출력
└── controller/
    └── MainController.java            ← 수정: sampleManagement() → SampleController 위임

src/test/java/org/example/
├── controller/
│   └── SampleControllerTest.java      ← 신규
└── view/
    └── SampleOutputViewTest.java      ← 신규
```

## 클래스 설계

### SampleController.java

```java
public class SampleController {
    // 생성자 주입: SampleRepository, SampleInputView, SampleOutputView

    public void run() {
        while (true) {
            out.showMenu();
            switch (in.readLine()) {
                case "1" -> register();
                case "2" -> listAll();
                case "3" -> search();
                case "0" -> { return; }  // 메인 메뉴로 복귀
                default  -> out.showError("올바른 번호를 입력하세요.");
            }
        }
    }

    private void register()  // 시료 등록
    private void listAll()   // 목록 조회
    private void search()    // 이름 검색
}
```

### SampleInputView.java

```java
public class SampleInputView {
    // 생성자: Scanner 주입 (MainController와 동일한 Scanner 공유 — 버퍼 충돌 방지)

    public String readId()               // 시료 ID 입력
    public String readName()             // 시료명 입력
    public double readAvgProductionTime()// 평균 생산시간 입력
    public double readYieldRate()        // 수율 입력
    public int    readStock()            // 초기 재고 입력
    public String readSearchKeyword()    // 검색 키워드 입력
    public String readLine()             // 메뉴 선택
}
```

### SampleOutputView.java

```java
public class SampleOutputView {
    // 생성자: PrintStream 주입 (테스트 가능하도록)

    public void showMenu()                         // 시료 관리 서브 메뉴
    public void showSampleList(List<Sample> list)  // 목록 테이블 출력
    public void showSampleDetail(Sample sample)    // 단일 시료 상세
    public void showRegisterSuccess(Sample sample) // 등록 완료 메시지
    public void showNotFound()                     // 검색 결과 없음
    public void showError(String msg)
}
```

### 목록 출력 형식 (예시)

```
등록 시료 목록 (총 3종)
------------------------------------------------------------
 ID       시료명                 평균생산시간   수율    현재재고
 S-001    실리콘 웨이퍼-8인치     0.5 min/ea   0.92    480 ea
 S-002    GaN 에피택셜-4인치      0.3 min/ea   0.78    220 ea
 S-003    SiC 파워기판-6인치      0.8 min/ea   0.92     30 ea
------------------------------------------------------------
```

### 등록 입력 흐름

```
시료 ID       > S-004
시료명        > 포토레지스트-PR7
평균 생산시간 > 0.2
수율          > 0.95
초기 재고     > 0

[등록 확인]
  ID: S-004 / 이름: 포토레지스트-PR7 / 생산시간: 0.2 min/ea / 수율: 0.95 / 재고: 0 ea
등록하시겠습니까? (Y/N) > Y
등록 완료: S-004 포토레지스트-PR7
```

## 비즈니스 규칙

- 동일 ID로 재등록 시 오류 메시지 출력 (덮어쓰기 금지)
- 수율은 0 초과 1 이하만 허용
- 평균 생산시간 / 재고는 0 이상 정수(또는 소수) 허용
- 검색은 시료명 부분 일치 (대소문자 무시)

## 테스트 계획

### SampleControllerTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `register_withValidInput_savesSample` | 유효한 입력으로 시료 등록 시 Repository에 저장됨 |
| `register_withDuplicateId_showsError` | 중복 ID 등록 시 오류 메시지 출력, 저장 안 됨 |
| `listAll_showsAllSamples` | 등록된 시료가 목록에 출력됨 |
| `listAll_whenEmpty_showsEmptyMessage` | 시료가 없을 때 빈 목록 메시지 출력 |
| `search_withMatchingKeyword_returnsSamples` | 키워드 일치 시료 반환 |
| `search_withNoMatch_showsNotFound` | 일치 없을 때 "검색 결과 없음" 출력 |

### SampleOutputViewTest

| 테스트 | 검증 내용 |
|--------|-----------|
| `showSampleList_formatsTableCorrectly` | 목록 출력에 ID·이름·재고가 포함됨 |
| `showRegisterSuccess_containsSampleInfo` | 등록 완료 메시지에 시료 ID와 이름이 포함됨 |
| `showNotFound_printsMessage` | "검색 결과 없음" 문구 포함 |

## MainController 수정 내용

```java
// 기존
private void sampleManagement() { out.showNotImplemented(); }

// 변경: SampleController로 위임
private void sampleManagement() { sampleController.run(); }
```

`SampleController`는 `MainController` 생성자에서 조립한다.

## 완료 기준

- `[1] 시료 관리` 진입 후 등록 / 조회 / 검색이 동작한다.
- 중복 ID, 잘못된 수율 입력에 오류 메시지가 출력된다.
- `./gradlew test` 전체 통과.
