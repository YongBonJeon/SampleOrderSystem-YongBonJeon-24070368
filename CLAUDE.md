# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**반도체 시료 생산주문관리 시스템** — 가상의 반도체 회사 S-Semi의 시료(Sample) 생산 및 주문을 관리하는 콘솔 기반 Java 애플리케이션.

기능 명세 및 도메인 상세는 [`docs/PRD.md`](docs/PRD.md)를 참고한다.

## Tech Stack

- **Language**: Java
- **Build**: Gradle
- **Test**: JUnit Jupiter 6
- **Architecture**: MVC (model / controller / view / repository 패키지 분리)
- **DB**: H2 (영속성 구현체 선택 가능 — FILE / JSON / DATABASE)

## Commands

```bash
# 빌드
./gradlew build

# 테스트 전체 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "org.example.ClassName"

# 단일 테스트 메서드 실행
./gradlew test --tests "org.example.ClassName.methodName"

# 애플리케이션 실행
./gradlew run
```

## 문서 동기화 규칙 (필수 — 생략 금지)

Phase 구현이 사용자에게 확인되면 **commit 전에 반드시** 아래 체크리스트를 실행한다.  
이 절차를 건너뛰고 commit하는 것은 금지한다.

### 체크리스트 (매 Phase 완료 시 순서대로)

1. `docs/PLAN.md` — 해당 Phase 진행 현황이 `✅ 완료`로 업데이트되었는가?
2. `docs/design/phaseN.md` — 실제 구현과 다른 내용(제거된 기능, 변경된 방식)이 있는가?
3. `CLAUDE.md` — 기술 스택·규칙 중 달라진 것이 있는가?
4. `docs/FEATURES/*.md`, `docs/PRD.md` — 구현이 문서 내용을 변경했는가?

### 결과 보고 형식

- 불일치 발견 시: 항목별로 사용자에게 보고하고, 수정 여부를 확인한 뒤 수정한다.
- 불일치 없음: "`.md` 파일 확인 완료 — 수정 불필요"라고 명시한다.

**commit은 이 절차 완료 후에만 실행한다.**

## TDD Workflow

이 프로젝트는 **RED → GREEN → REVIEW** 사이클로 TDD를 진행한다.

```
/tdd   ← 사이클 가이드 스킬 (`.claude/commands/tdd.md`)
```

- **RED**: Plan.md 작성 → 사람 검토 승인 → 테스트 작성
- **GREEN**: 테스트를 통과시키는 최소 구현
- **REVIEW**: 코드 검토 → 사람 검토 승인 → 다음 사이클

RED와 REVIEW 단계는 사람의 명시적 승인 없이 다음 단계로 넘어가지 않는다.

## Behavioral Guidelines

### 1. Think Before Coding

Before implementing:
- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

### 3. Surgical Changes

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting unless asked.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it — don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that **your** changes made unused.
- Don't remove pre-existing dead code unless asked.

### 4. Goal-Driven Execution

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
```
