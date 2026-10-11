---
name: tdd-vibe-coding
description: Test-driven development for Vibe Coding. Use when the user wants to build features or fix bugs from a PRD/Spec, mentions "test-first" or "red-green-refactor", or wants the AI agent to generate test cases from a PRD before writing code. Adapted for Spring Boot + JUnit 5 (backend) and miniprogram / Jest (frontend).
---

# TDD for Vibe Coding

TDD is the red → green loop, but for **AI-human collaboration** it has an extra layer: **the PRD must come first**. Without a PRD, the test cases you generate are imagined, not specified.

This skill supports **two scenarios**:

| Scenario | Description | Stage 3 Behavior |
|----------|-------------|------------------|
| **Green Field** | New feature development, code does not exist | Write code to make TC pass (red→green) |
| **Retro-test** | Existing business code, need to verify | Read code and verify against TC spec |

When exploring the codebase, read `references/glossary.md` (if it exists) so test names and interface vocabulary match the project's domain language.

## The 4-Stage Workflow

```
[Stage 1] PRD / Spec document            (human provides)
       ↓
[Stage 2] Test Cases from PRD           (agent generates)
       ↓
[Stage 3] Implementation makes Tests pass
       │  ┌─ Green Field: write code → TC pass (red→green)
       └──┤
       │  └─ Retro-test: read code → verify TC (record results)
       ↓
[Stage 4] Test Report + Sign-off        (human reviews)
       ↓
    [FAIL] → back to Stage 3 (fix bug, rerun tests)
    [PASS] → next feature
```

**The most important rule**: Stage 1 is non-negotiable. Never start Stage 2 without a PRD. The PRD is the **independent source of truth** for what the tests should verify — without it, tests become tautological (they restate the implementation, see [anti-patterns.md](references/anti-patterns.md)).

---

## ⚠️ Output Boundaries by Stage

**Must be strictly separated**, otherwise TC documents will be mixed with verification results:

| Stage | Output | Must Contain | Must NOT Contain |
|-------|--------|-------------|-----------------|
| **Stage 2** | TC Document | Metadata + Specification (Preconditions / Test Steps / Expected Results / **Failure Handling**) + **Code Template** (per test-case-template.md) | Verification Evidence, Status |
| **Stage 3** | Verification Record (Retro-test) | TC verification results (PASS/FAIL/N/A) + evidence | TC spec content |
| **Stage 4** | Test Report | Summary + sign-off + defect list + open items | Full TC content (only reference TC IDs) |

**Common mistakes**:
- ❌ Writing "Verification Evidence" into the TC document
- ❌ Writing "Status (PASS/FAIL)" into the TC document
- ❌ Writing full TC content into the test report (only reference TC IDs)

**Correct approach**:
- ✅ TC document = specification (**what should** happen) + **Code Template** (executable form)
- ✅ Verification record = results (**what actually** happens)
- ✅ Test report = summary + evidence + sign-off

---

## Stage 1 · PRD / Spec (Human → Agent)

The PRD must contain:

- **Functional requirements** (FR-1, FR-2, ...): what the system does
- **Non-functional requirements** (NFR-1, NFR-2, ...): performance, security, error handling
- **API contracts** (if any): endpoints, request/response shape, status codes
- **Data model** (if any): entities, fields, relationships
- **Acceptance criteria**: how to know the feature is "done"

See [prd-template.md](references/prd-template.md) for a full template.

**What you must NOT do**: skip the PRD and go straight to "write me some tests." That produces tests for imagined behavior, not specified behavior.

---

## Stage 2 · Test Cases from PRD (Agent → Human)

**Coverage goal: 100% of PRD's functional requirements.** For each FR-N, generate at least one Test Case.

**Test Case types**:

| Type | What it covers | Example |
|------|---------------|---------|
| `normal` | Happy path | "user can create an order with valid cart" |
| `exception` | Failure paths | "user cannot create order when payment service is down" |
| `boundary` | Edge values | "empty cart, max cart size, zero price" |
| `reverse` | Things that shouldn't happen | "user cannot delete another user's order" |
| `stress` | Volume / concurrency | "1000 concurrent checkouts, no data loss" |

**Type applicability rule (V4)**: not every type applies to every project. For each absent type you MUST write one explicit line in the TC document:

- `stress`：requires concurrent/stress testing environment. **Pure client-side projects (WeChat Mini Program / H5 standalone) must explicitly declare `stress: N/A — client-side has no concurrent scenario`**; server-side API projects require it.
- `reverse`：required when multi-user / permission / data isolation is involved; pure standalone single-user features can declare N/A with explanation.
- Declaring N/A ≠ ignoring: must write explicit N/A reason in the TC document summary table.

**⚠️ Time/Performance number extraction rule (V4)**：Time commitments in PRD acceptance criteria (e.g. "within 5 seconds", "complete within 10 seconds") are **functional acceptance conditions**, not stress. Even without a separate NFR section in the PRD, every time commitment must be extracted as a normal TC (test steps include timing method), with second count upper limit and timeout behavior in Expected Results.

See [test-case-template.md](references/test-case-template.md) for the full schema.

**⚠️ Code Template requirements**:

Each TC must contain a **Code Template**, the executable form of the test:

| Type | Code Template | Notes |
|------|-------------|-------|
| Java backend | ✅ Executable | JUnit 5 test code, can run directly |
| Mini Program frontend | ⚠️ Reference only | No official test framework, requires manual verification |

**Important**:
- Code Template is the executable form of the TC spec, **not optional**
- Verification method depends on project tech stack:
  - Has test framework (Java) → run tests
  - No test framework (Mini Program) → manual verification
- Code Template should indicate verification method and expected results

**Test naming convention** (when writing the actual code):
```
TC-{ModuleName}-{SequenceNumber}-{Type}
```
Examples:
- `TC-BPM-001-normal` — first normal test for BPM module
- `TC-BPM-007-boundary` — seventh boundary test
- `TC-OrderCheckout-013-stress` — thirteenth stress test

---

## Stage 3 · Implementation (Agent → Tests Pass)

### Decision Point: Green Field or Retro-test?

Before Stage 3, determine the scenario:

```
Does business code exist?
├─ No → Green Field: new feature development, jump to "Green Field Implementation"
└─ Yes → Retro-test: existing business code, jump to "Retro-test Verification"
```

---

### Green Field Implementation (Code Does Not Exist)

**Vertical slicing** (not horizontal). One test → one minimal implementation → next test. Do not write all tests first, then all code.

**Framework**:
- **Backend (Spring Boot)**: JUnit 5 + Mockito + Spring Test
- **Frontend (WeChat Mini Program)**: miniprogram test framework
- **Frontend (Web)**: Jest + Testing Library

**Mock boundaries** (see [mocking.md](references/mocking.md)):
- Mock at system boundaries: external APIs, databases, time, file system
- Do NOT mock: your own classes, internal collaborators, anything you control
- Prefer dependency injection so mocks are easy

**Green Field steps**:
1. Pick the first TC (highest priority normal case)
2. Write the test code → verify it FAILS (red)
3. Write the minimal implementation → verify it PASSES (green)
4. Refactor if needed
5. Repeat for next TC
6. Record each red→green cycle in the Cycle Log

---

### Retro-test Verification (Existing Business Code)

**Important note**: Retro-test Stage 3 is "verify existing code", not "write code to make tests pass". The existing code behavior is fixed; TCs are the verification standard.

**Retro-test steps**:
1. Pick a TC from Stage 2
2. **Read the code** and **run the test** (or manually verify)
   - If test code exists (JUnit/Jest): Run it directly → record pass/fail
   - If test code doesn't exist: Manually verify by:
     a. Reading the existing code
     b. Following the TC's test steps
     c. Checking expected results
     d. Recording result with evidence
3. If FAIL: Record as defect (D-ID, Severity, Description)
4. Repeat for all TCs
5. Generate defect list with evidence (code snippets, screenshots, logs)

**Three-state judgment rule (V4)** — Retro-test results have only three states, binary hard judgment is forbidden:

| Result | Judgment Criteria |
|--------|------------------|
| **PASS** | Automated test passes; **or** pure logic TC confirmed by source code review that implementation matches Expected Results, and behavior **does not depend on real device rendering/timing** |
| **FAIL** | Corresponding implementation not found in source, or implementation clearly contradicts Expected Results → also record defect |
| **N/A (pending manual/real-device test)** | Expected result depends on real device behavior: scroll positioning, animations, layout occlusion, performance timing, permission dialogs, recording/camera hardware, etc. Code layer can only confirm "logic exists", cannot confirm "real device is correct" |

> Lesson: "compensation logic exists in code" alone cannot be judged PASS — real device rendering timing may cause logic to fail (2026-10 pull-up-to-load jump-to-bottom case).

**Minimum evidence standard (V4)**: Each verification record's evidence must include at least one `file-relative-path:line-number` reference; "logic exists" does not constitute sufficient evidence, must explain which path was traced and which Expected Result was checked against.

**Retro-test verification record format**:
```markdown
## TC Verification Record

| TC ID | Verification Time | Verification Method | Result | Evidence | Notes |
|-------|-----------------|--------------------|--------|----------|-------|
| TC-XXX-001 | 2026-10-10 | Run test | PASS | XxxTest.java:42 passed | JUnit |
| TC-XXX-002 | 2026-10-10 | Manual verification | FAIL | pages/x/index.js:88 no null check | D-001 |
| TC-XXX-003 | 2026-10-10 | Source code review | N/A | index.js:240 has compensation logic | Scroll positioning, pending real device |
```

**⚠️ Retro-test precautions**:
- Do not modify existing code to "make tests pass" (unless user explicitly requests fixing)
- Verification is one-way: TCs are the standard, existing code is the verified object
- Items that cannot be confirmed at code layer (real device/hardware/timing) → mark **N/A (pending manual test)**, do not hard-label PASS for the sake of pass rate
- **Verification Evidence goes in the verification record or report, NOT in the TC document**

---

### Mock Boundaries (Shared)

- Mock at system boundaries: external APIs, databases (use test DB if possible), time, file system
- Do NOT mock: your own classes, internal collaborators, anything you control
- Prefer dependency injection so mocks are easy

---

## Stage 4 · Test Report (Agent → Human)

After all TCs are verified (Green Field: all pass; Retro-test: all verified), generate a Test Report.

**Green Field report** must include:
- **Test case count** by type (normal/exception/boundary/reverse/stress)
- **Pass/fail summary**
- **Coverage report**:
  - Scenario coverage: which PRD FRs are covered by tests (must be 100%)
  - Code coverage: JaCoCo (Java) / istanbul (JS) — target 80%+ for critical paths
- **Defect list** (if any): bugs found, severity, status
- **Time to green**: how many red→green cycles

**Retro-test report** must include:
- **Test case count** by type (including explicit N/A type declaration row)
- **Verification results summary**: passed / failed / **N/A (pending manual/real-device)** / unable to verify
- **Coverage report**:
  - Scenario coverage: which PRD FRs have corresponding code
  - Code coverage: Give number if measurable; **when project has no coverage tool (e.g. Mini Program), keep section and fill N/A + reason, do not delete section or fabricate percentage**
- **Defect list**: bugs found, severity, status, evidence (code snippets)
- **Verification evidence**: how each TC was verified (test run / source code review / pending real device)
- **Open items / 待办**: TCs not verified on real device, modules without test framework, visual verification gaps

**Report status rules (V4)**:
- `PASS`: no FAIL, no open P0/P1, no pending critical N/A TCs
- `FAIL`: exists FAIL
- `INCOMPLETE`: exists N/A pending real device or open P1 — **this verification cycle is in this state, do not label PASS just to look good**

See [report-template.md](references/report-template.md) for the full schema.

---

## Known Capability Boundaries & Open Items (V4 Honest Declaration)

| Capability | Java/Spring Backend | Web/H5 | WeChat Mini Program |
|------------|---------------------|---------|--------------------|
| Automated unit testing | ✅ JUnit 5 executable | ✅ Jest executable | ❌ No official framework, Code Template is manual verification placeholder only |
| UI / E2E visual verification | — | ✅ Playwright can screenshot compare | ⚠️ WeChat DevTools only, AI cannot directly observe real device rendering compliance |
| Coverage statistics | ✅ JaCoCo | ✅ istanbul | ❌ Cannot auto-stat |
| Real device behavior (scroll/animation/hardware/timing) | N/A | ⚠️ Partially simulatable | ❌ Must be manually verified on real device |

**Open items (register by project, do not pretend to cover)**:
1. WeChat Mini Program test framework selection (miniprogram-simulate / cloud real-device automation) → fill in executable Code Template
2. Real device visual verification pipeline (H5/Android/iOS can connect AI screenshot compare; Mini Program waits for WeChat ecosystem capability)
3. Performance timing TCs (5s/10s/15s) can only be closed with real device timing

---

## Anti-patterns

See [anti-patterns.md](references/anti-patterns.md) for the full list. Top 5 to avoid:

1. **Tests without PRD** — writing tests for imagined behavior. Always start with a PRD.
2. **Implementation-coupled tests** — mocking internal collaborators, testing private methods. Test through public interfaces.
3. **Tautological tests** — `expect(add(a, b)).toBe(a + b)`. Expected values must come from an independent source of truth (the PRD).
4. **Horizontal slicing** — writing all tests first, then all code. Use vertical slicing: one test → one implementation.
5. **100% code coverage as goal** — code coverage is a means, not an end. **Scenario coverage (FR → TC mapping) is the real goal**.

---

## When to Invoke Related Skills

- **`code-review`**: after Stage 4, before final sign-off. The TDD skill gets tests green; `code-review` checks refactoring opportunities.
- **`codebase-design`**: when the seam (public interface) itself is in question. Consult, don't run as a session.
- **`grill-me`**: when the PRD feels too vague. Pressure-test the requirements before Stage 2.
- **`domain-modeling`**: when test names don't match the project's domain language. Align vocabulary first.

---

## Quick Start

### Green Field (New Feature Development)

1. Read [workflow-checklist.md](references/workflow-checklist.md) for the 1-page summary
2. Read [prd-template.md](references/prd-template.md) and fill it in for your feature
3. Hand the PRD to the agent with: "Generate test cases from this PRD using the tdd-vibe-coding skill"
4. The agent generates Test Cases with Code Templates (Stage 2)
5. Review the Test Cases against the PRD (Stage 2 sign-off)
6. The agent writes implementation code to make tests pass (Stage 3)
7. The agent generates a Test Report (Stage 4)
8. You sign off, or loop back to Stage 3 if tests fail

### Retro-test (Existing Business Code Verification)

1. Read [workflow-checklist.md](references/workflow-checklist.md) for the 1-page summary
2. Read [prd-template.md](references/prd-template.md) and fill it in for your feature
3. Hand the PRD + codebase to the agent with: "Verify existing code against this PRD using the tdd-vibe-coding skill (Retro-test mode)"
4. The agent generates Test Cases with Code Templates (Stage 2)
5. Review the Test Cases against the PRD (Stage 2 sign-off)
6. The agent verifies each TC against existing code, records pass/fail (Stage 3)
7. The agent generates a Verification Report with defect list (Stage 4)
8. You sign off, or request fixes for P0/P1 defects

---

## Scenario Decision Tree

```
User requests TDD
    │
    ├─ "New feature" / "from scratch" / "code does not exist"
    │   └─→ Green Field: new feature development
    │
    ├─ "Existing code" / "verify" / "test existing code" / "regression test"
    │   └─→ Retro-test: existing business code verification
    │
    └─ Not sure?
        └─→ Ask user: "Is this code already existing, or does it need to be created?"
```
