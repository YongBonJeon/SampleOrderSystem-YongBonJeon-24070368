---
name: test-driven-development
description: 모든 기능 개발 또는 버그 수정 시 구현 코드를 작성하기 전에 사용 (Java + Gradle + JUnit 5)
---

# 테스트 주도 개발 (TDD) — Java / Gradle / JUnit 5

## 개요

테스트를 먼저 작성한다. 실패하는 것을 확인한다. 통과시킬 최소한의 코드를 작성한다. 사람이 검토한다.

**핵심 원칙:** 테스트가 실패하는 것을 직접 보지 않았다면, 그 테스트가 올바른 것을 검증하는지 알 수 없다.

**규칙의 문구를 어기는 것은 규칙의 정신을 어기는 것이다.**

## 언제 사용하는가

**항상:**
- 새로운 기능
- 버그 수정
- 리팩터링
- 동작 변경

**예외 (사용자에게 확인 필요):**
- 일회성 프로토타입
- 자동 생성된 코드
- 설정 파일

"이번 한 번만 TDD를 건너뛰자"는 생각이 든다면? 멈춰라. 그것은 합리화다.

## 절대 법칙

```
실패하는 테스트 없이 프로덕션 코드를 작성하지 말 것
RED와 REVIEW 단계는 반드시 사람의 검토를 거쳐야 한다
```

테스트보다 코드를 먼저 작성했는가? 삭제하라. 처음부터 다시 시작하라.

**예외 없음:**
- "참고용"으로 보관하지 마라
- 테스트를 작성하면서 그 코드를 "각색"하지 마라
- 그 코드를 보지 마라
- 삭제는 삭제다

테스트로부터 새롭게 구현하라. 끝.

## Red-Green-Review

### RED — 테스트 계획 수립 및 작성

#### STEP 1: docs/design/phaseN.md 작성 (사람 검토 필수)

테스트를 작성하기 전에, 무엇을 테스트할지 계획을 먼저 `docs/design/phaseN.md`에 작성한다.  
N은 현재 작업 중인 Phase 번호다 (예: Phase 3이면 `docs/design/phase3.md`).  
파일이 이미 존재하면 기존 내용을 유지하고 TDD 사이클 계획 섹션을 추가한다.

**docs/design/phaseN.md 추가 형식:**
```markdown
# TDD Plan — [기능/버그명]

## 목표
[이 사이클에서 검증하려는 단 하나의 동작]

## 테스트 설계
- 테스트 이름: [명확한 메서드명]
- @DisplayName: [한글로 동작 설명]
- Given: [초기 상태]
- When: [실행할 동작]
- Then: [기대하는 결과]

## 엣지 케이스 (이번 사이클 범위 외)
- [나중에 다룰 케이스들]
```

**⛔ docs/design/phaseN.md 작성 후 반드시 멈춰라.**

```
docs/design/phaseN.md를 작성했습니다. 검토 후 진행을 허락해 주세요.
```

사람의 명시적 승인(예: "진행해", "ok", "좋아") 없이는 테스트 코드를 작성하지 않는다.

---

#### STEP 2: 테스트 작성 (승인 후)

승인을 받은 뒤, docs/design/phaseN.md의 설계대로 테스트 하나를 작성한다.

**좋은 예:**
```java
@Test
@DisplayName("실패한 작업을 3번 재시도한다")
void retriesFailedOperationsThreeTimes() {
    AtomicInteger attempts = new AtomicInteger(0);
    Supplier<String> operation = () -> {
        int current = attempts.incrementAndGet();
        if (current < 3) throw new RuntimeException("fail");
        return "success";
    };

    String result = RetryHelper.retryOperation(operation);

    assertEquals("success", result);
    assertEquals(3, attempts.get());
}
```
명확한 이름, 실제 동작 검증, 한 가지만 테스트

**나쁜 예:**
```java
@Test
void retryWorks() {
    @SuppressWarnings("unchecked")
    Supplier<String> mock = mock(Supplier.class);
    when(mock.get())
        .thenThrow(new RuntimeException())
        .thenThrow(new RuntimeException())
        .thenReturn("success");

    RetryHelper.retryOperation(mock);

    verify(mock, times(3)).get();
}
```
모호한 이름, 실제 코드가 아닌 mock을 검증

**요구사항:**
- 하나의 동작
- 명확한 이름
- 실제 코드 사용 (불가피하지 않다면 mock 사용 금지)

#### RED 검증 — 실패하는 것을 직접 본다

**필수. 절대 건너뛰지 말 것.**

```bash
./gradlew test --tests "com.example.RetryHelperTest.retriesFailedOperationsThreeTimes"
```

확인할 것:
- 테스트가 실패하는가 (오류가 아닌)
- 실패 메시지가 예상한 그대로인가
- 기능이 없어서 실패하는가 (오타 때문이 아닌)

**테스트가 통과한다고?** 이미 존재하는 동작을 테스트하고 있는 것이다. 테스트를 고쳐라.

**테스트가 컴파일/실행 오류를 낸다고?** 오류를 고치고, 올바르게 실패할 때까지 다시 실행하라.

---

### GREEN — 최소 구현 코드

테스트를 통과시킬 가장 단순한 코드를 작성한다.

**좋은 예:**
```java
public final class RetryHelper {
    private RetryHelper() {}

    public static <T> T retryOperation(Supplier<T> fn) {
        for (int i = 0; i < 3; i++) {
            try {
                return fn.get();
            } catch (RuntimeException e) {
                if (i == 2) throw e;
            }
        }
        throw new IllegalStateException("unreachable");
    }
}
```
통과시킬 만큼만

**나쁜 예:**
```java
public final class RetryHelper {
    public static <T> T retryOperation(
        Supplier<T> fn,
        int maxRetries,
        BackoffStrategy backoff,
        Consumer<Integer> onRetry,
        Predicate<Throwable> retryOn
    ) {
        // YAGNI — 지금 필요 없음
    }
}
```
과도한 설계

기능을 추가하지 마라, 다른 코드를 리팩터링하지 마라, 테스트가 요구하는 것 이상으로 "개선"하지 마라.

#### GREEN 검증 — 통과하는 것을 직접 본다

**필수.**

```bash
./gradlew test --tests "com.example.RetryHelperTest.retriesFailedOperationsThreeTimes"
```

확인할 것:
- 테스트가 통과하는가
- 다른 테스트도 여전히 통과하는가 (`./gradlew test`로 전체 실행)
- 출력이 깨끗한가 (오류, 경고 없음)

**테스트가 실패한다고?** 코드를 고쳐라. 테스트가 아니다.

**다른 테스트가 깨졌다고?** 지금 고쳐라.

---

### REVIEW — 코드 검토 및 다음 사이클 결정 (사람 검토 필수)

GREEN 상태가 확인되면 즉시 멈추고 사람에게 검토를 요청한다.

#### STEP 1: 검토 보고서 작성

다음 내용을 정리해서 사람에게 제시한다:

```
## REVIEW — [기능/버그명]

### 이번 사이클 요약
- 테스트: [테스트명]
- 구현: [무엇을 구현했는가]
- 테스트 결과: PASS ✅

### 코드 상태
[구현한 프로덕션 코드 또는 주요 변경사항]

### 정리 제안 (선택)
- [중복 제거, 이름 개선, 헬퍼 추출 등 — 동작 변경 없음]

### 다음 사이클 후보
1. [다음에 다룰 동작 후보 1]
2. [다음에 다룰 동작 후보 2]
3. [다음에 다룰 동작 후보 3]
```

**⛔ 검토 보고서 작성 후 반드시 멈춰라.**

```
REVIEW 보고서를 작성했습니다. 검토 후 다음 중 하나를 알려주세요:
- 정리 진행 → 정리 내용 승인 또는 수정
- 다음 사이클 진행 → 위 후보 중 선택 또는 새로운 목표 지시
- 완료 → 작업 종료
```

사람의 명시적 지시 없이는 다음 사이클의 docs/design/phaseN.md를 작성하지 않는다.

#### STEP 2: 승인된 정리 진행 (선택)

사람이 정리를 승인했다면:
- 중복 제거
- 이름 개선
- 헬퍼 추출

테스트는 계속 그린 상태로 유지한다. 동작을 추가하지 않는다.

#### STEP 3: 다음 사이클

사람이 다음 동작을 지시하면 RED 단계(docs/design/phaseN.md)부터 다시 시작한다.

---

## 사이클 흐름 요약

```
RED ─── docs/design/phaseN.md 작성
         ↓
    ⛔ 사람 검토 대기 ──→ 수정 요청 시 docs/design/phaseN.md 수정
         ↓ 승인
    테스트 작성
         ↓
    실패 확인 (gradlew test)
         ↓
GREEN ── 최소 구현
         ↓
    통과 확인 (gradlew test)
         ↓
REVIEW ─ 검토 보고서 작성
         ↓
    ⛔ 사람 검토 대기 ──→ 정리 지시 시 정리 후 대기
         ↓ 다음 사이클 지시
    RED (docs/design/phaseN.md) 로 돌아감
```

---

## 좋은 테스트

| 품질 | 좋음 | 나쁨 |
|------|------|------|
| **최소** | 한 가지만. 이름에 "and"가 있나? 분리하라. | `validatesEmailAndDomainAndWhitespace` |
| **명확** | 이름이 동작을 설명한다 | `test1`, `testHelper` |
| **의도 표현** | 원하는 API를 보여준다 | 코드가 어떻게 동작해야 하는지 흐려놓는다 |

JUnit 5에서는 `@DisplayName`을 활용해 한글로 의도를 명확하게 표현할 수 있다.

## 순서가 중요한 이유

**"코드 작성 후 테스트로 검증하면 되지 않나"**

코드 작성 후 작성한 테스트는 즉시 통과한다. 즉시 통과하는 것은 아무것도 증명하지 않는다:
- 잘못된 것을 테스트했을 수 있다
- 동작이 아닌 구현을 테스트했을 수 있다
- 잊어버린 엣지 케이스를 놓쳤을 수 있다
- 그 테스트가 실제로 버그를 잡는 것을 본 적이 없다

테스트 우선은 테스트가 실패하는 것을 직접 보게 만들어, 실제로 무언가를 검증한다는 사실을 증명한다.

**"엣지 케이스는 이미 수동으로 다 테스트했다"**

수동 테스트는 즉흥적이다. 모두 테스트했다고 생각하지만:
- 무엇을 테스트했는지 기록이 없다
- 코드가 변경되면 다시 실행할 수 없다
- 압박 상황에서는 케이스를 잊기 쉽다
- "내가 시도했을 때는 됐다" ≠ 포괄적

자동 테스트는 체계적이다. 매번 동일하게 실행된다.

**"X시간의 작업을 지우는 것은 낭비다"**

매몰비용 오류다. 그 시간은 이미 지나갔다. 지금의 선택지:
- 삭제하고 TDD로 재작성 (X시간 추가, 높은 신뢰도)
- 그대로 두고 사후 테스트 추가 (30분, 낮은 신뢰도, 버그 가능성 높음)

"낭비"는 신뢰할 수 없는 코드를 그대로 두는 것이다. 진짜 테스트가 없는 동작 코드는 기술 부채다.

**"TDD는 교조적이다, 실용주의는 적응하는 것이다"**

TDD가 곧 실용적이다:
- 커밋 전에 버그를 찾는다 (사후 디버깅보다 빠르다)
- 회귀를 방지한다 (테스트가 즉시 깨짐을 잡아낸다)
- 동작을 문서화한다 (테스트가 사용법을 보여준다)
- 리팩터링을 가능하게 한다 (자유롭게 변경, 테스트가 깨짐을 잡는다)

"실용적인" 지름길 = 운영 환경 디버깅 = 더 느려진다.

**"사후 테스트도 같은 목표를 달성한다 — 형식이 아닌 정신이다"**

아니다. 사후 테스트는 "이 코드가 무엇을 하는가?"에 답한다. 우선 테스트는 "이 코드가 무엇을 해야 하는가?"에 답한다.

사후 테스트는 당신의 구현에 편향된다. 요구사항이 아닌 만든 것을 테스트한다. 발견한 엣지 케이스가 아닌 기억나는 엣지 케이스를 검증한다.

우선 테스트는 구현 전에 엣지 케이스 발견을 강제한다. 사후 테스트는 모든 것을 기억했는지 검증할 뿐이다 (기억하지 못한다).

사후 30분의 테스트 ≠ TDD. 커버리지는 얻지만 테스트가 작동한다는 증명은 잃는다.

## 흔한 합리화

| 변명 | 현실 |
|------|------|
| "테스트하기엔 너무 단순하다" | 단순한 코드도 깨진다. 테스트는 30초면 된다. |
| "나중에 테스트하겠다" | 즉시 통과하는 테스트는 아무것도 증명하지 않는다. |
| "사후 테스트도 같은 목표를 달성한다" | 사후 = "이 코드가 무엇을 하는가?", 우선 = "무엇을 해야 하는가?" |
| "이미 수동으로 테스트했다" | 즉흥적 ≠ 체계적. 기록이 없고, 다시 실행할 수 없다. |
| "X시간을 지우는 것은 낭비" | 매몰비용 오류. 검증되지 않은 코드를 두는 것이 기술 부채다. |
| "참고용으로 두고 테스트 먼저 작성한다" | 그것을 각색하게 된다. 그건 사후 테스트다. 삭제는 삭제다. |
| "먼저 탐색해야 한다" | 좋다. 탐색 코드는 버리고, TDD로 시작하라. |
| "테스트하기 어렵다 = 설계가 불명확하다" | 테스트의 말을 들어라. 테스트하기 어려우면 사용하기도 어렵다. |
| "TDD는 나를 느리게 한다" | TDD는 디버깅보다 빠르다. 실용적 = 테스트 우선. |
| "수동 테스트가 더 빠르다" | 수동은 엣지 케이스를 증명하지 않는다. 변경할 때마다 재테스트해야 한다. |
| "기존 코드에 테스트가 없다" | 당신이 그것을 개선하는 중이다. 기존 코드에도 테스트를 추가하라. |
| "docs/design/phaseN.md 없이 바로 테스트 쓰면 되지 않나" | 계획 없는 테스트는 범위를 벗어나기 쉽다. 사람의 눈으로 먼저 검토한다. |
| "REVIEW는 형식적이다" | REVIEW가 다음 사이클의 방향을 결정한다. 사람의 판단이 필요하다. |

## 위험 신호 — 멈추고 처음부터 다시 시작

- 테스트보다 먼저 작성된 코드
- 구현 후 작성된 테스트
- 테스트가 즉시 통과
- 테스트가 왜 실패했는지 설명할 수 없음
- 테스트를 "나중에" 추가
- docs/design/phaseN.md 없이 테스트 작성
- 사람의 승인 없이 docs/design/phaseN.md → 테스트 작성으로 넘어감
- 사람의 지시 없이 REVIEW → 다음 RED로 넘어감
- "이번 한 번만"이라는 합리화
- "이미 수동으로 테스트했다"
- "사후 테스트도 같은 목적을 달성한다"
- "형식이 아니라 정신이다"
- "참고용으로 두자" 또는 "기존 코드를 각색하자"
- "이미 X시간 썼는데 지우는 건 낭비다"
- "TDD는 교조적이다, 나는 실용적이다"
- "이건 다르다, 왜냐하면…"

**이 모든 것의 의미: 코드를 삭제하라. TDD로 처음부터 다시 시작하라.**

## 예시: 버그 수정

**버그:** 빈 이메일이 허용됨

**RED — docs/design/phaseN.md**
```markdown
# TDD Plan — 빈 이메일 거부

## 목표
빈 이메일 입력 시 오류 메시지를 반환한다

## 테스트 설계
- 테스트 이름: rejectsEmptyEmail
- @DisplayName: 빈 이메일을 거부한다
- Given: email이 빈 문자열인 FormData
- When: FormService.submitForm(data) 호출
- Then: result.getError() == "Email required"

## 엣지 케이스 (이번 사이클 범위 외)
- null 이메일
- 공백만 있는 이메일
```

```
docs/design/phaseN.md를 작성했습니다. 검토 후 진행을 허락해 주세요.
```

*(승인 후)*

**RED — 테스트 작성 및 실패 확인**
```java
@Test
@DisplayName("빈 이메일을 거부한다")
void rejectsEmptyEmail() {
    FormData data = new FormData("");

    Result result = FormService.submitForm(data);

    assertEquals("Email required", result.getError());
}
```

```bash
$ ./gradlew test --tests "com.example.FormServiceTest.rejectsEmptyEmail"
FAILED
expected: "Email required" but was: null
```

**GREEN — 최소 구현 및 통과 확인**
```java
public final class FormService {
    public static Result submitForm(FormData data) {
        if (data.getEmail() == null || data.getEmail().trim().isEmpty()) {
            return Result.error("Email required");
        }
        return Result.ok();
    }
}
```

```bash
$ ./gradlew test --tests "com.example.FormServiceTest.rejectsEmptyEmail"
BUILD SUCCESSFUL
```

**REVIEW — 검토 보고서**
```
## REVIEW — 빈 이메일 거부

### 이번 사이클 요약
- 테스트: rejectsEmptyEmail
- 구현: 빈 문자열 검증 추가
- 테스트 결과: PASS ✅

### 코드 상태
FormService.submitForm에 이메일 빈값 검증 추가

### 정리 제안 (선택)
- 없음

### 다음 사이클 후보
1. null 이메일 거부
2. 공백만 있는 이메일 거부
3. 유효한 이메일 통과
```

```
REVIEW 보고서를 작성했습니다. 검토 후 다음 중 하나를 알려주세요:
- 정리 진행 → 정리 내용 승인 또는 수정
- 다음 사이클 진행 → 위 후보 중 선택 또는 새로운 목표 지시
- 완료 → 작업 종료
```

## Gradle / JUnit 5 실용 명령어

```bash
# 단일 테스트 메서드 실행
./gradlew test --tests "org.example.ClassName.methodName"

# 단일 클래스 실행
./gradlew test --tests "org.example.ClassName"

# 패키지 단위 실행
./gradlew test --tests "org.example.*"

# 테스트만 다시 실행 (캐시 무시)
./gradlew test --rerun-tasks

# 실패한 테스트만 자세히 보기
./gradlew test --info

# 테스트 리포트 위치
# build/reports/tests/test/index.html
```

## 검증 체크리스트

작업을 완료로 표시하기 전에:

- [ ] docs/design/phaseN.md를 작성하고 사람의 승인을 받았다
- [ ] 모든 새 메서드/클래스에 테스트가 있다
- [ ] 각 테스트가 구현 전에 실패하는 것을 직접 보았다
- [ ] 각 테스트가 예상한 이유로 실패했다 (오타가 아닌 기능 부재)
- [ ] 각 테스트를 통과시킬 최소 코드를 작성했다
- [ ] 모든 테스트가 통과한다 (`./gradlew test`)
- [ ] 출력이 깨끗하다 (오류, 경고 없음)
- [ ] REVIEW 보고서를 작성하고 사람의 지시를 받았다
- [ ] 테스트가 실제 코드를 사용한다 (mock은 불가피한 경우만)
- [ ] 엣지 케이스와 오류 경로가 다뤄졌다

모두 체크할 수 없다면? TDD를 건너뛴 것이다. 처음부터 다시 시작하라.

## 막힐 때

| 문제 | 해결 |
|------|------|
| 어떻게 테스트할지 모르겠다 | docs/design/phaseN.md에 먼저 적어보라. assertion부터 작성하라. 사람에게 물어보라. |
| 테스트가 너무 복잡하다 | 설계가 너무 복잡하다. 인터페이스를 단순화하라. |
| 모든 것을 mock해야 한다 | 코드가 너무 결합되어 있다. 의존성 주입을 사용하라. |
| 테스트 셋업이 너무 크다 | 헬퍼를 추출하라. 그래도 복잡하면 설계를 단순화하라. |
| docs/design/phaseN.md 범위가 너무 크다 | 한 사이클 = 하나의 동작. 범위를 줄여라. |

## 디버깅과의 통합

버그를 발견했나? Plan.md에 재현 시나리오를 작성하고 사람의 승인을 받아라. TDD 사이클을 따른다. 테스트가 수정을 증명하고 회귀를 방지한다.

테스트 없이 버그를 고치지 마라.

## 테스트 안티패턴

mock이나 테스트 유틸리티를 추가할 때, 흔한 함정을 피하기 위해 점검하라:
- 실제 동작이 아닌 mock의 동작을 테스트하기
- 프로덕션 클래스에 테스트 전용 메서드 추가하기
- 의존성을 이해하지 않고 mock하기

JUnit 5에서 추가로 유용한 것들:
- `@ParameterizedTest` — 같은 동작을 여러 입력으로 검증할 때 사용
- `@Nested` — 관련 테스트를 그룹화하여 의도를 명확히
- `assertAll(...)` — 한 번에 여러 assertion을 실행해 모든 실패를 보고
- `assertThrows(...)` — 예외 동작을 명시적으로 검증

## 최종 규칙

```
프로덕션 코드 → 먼저 실패한 테스트가 존재한다
RED 단계 → docs/design/phaseN.md 승인 없이 테스트를 작성하지 않는다
REVIEW 단계 → 사람의 지시 없이 다음 사이클을 시작하지 않는다
```

사용자의 명시적인 허락 없이는 예외 없음.
