# TDD for Vibe Coding · Workflow Checklist (1-Page Summary)

> **Print this page.** Use it for every feature. It is the 4 stages of the loop, in 1 page.
> **V5 sync**: This checklist is aligned with SKILL.md V5 rules (type N/A, Retro-test three-state, INCOMPLETE, honest Code Coverage).

---

## Stage 1 · PRD / Spec (Human → Agent)

- [ ] **PRD exists and is signed off** before Stage 2 begins
- [ ] PRD contains: FRs (functional requirements) + NFRs (non-functional) + API contracts + data model + acceptance criteria
- [ ] PRD uses the [prd-template.md](prd-template.md)
- [ ] PRD links to relevant GLOSSARY.md / ADR documents

**If PRD is vague or missing → STOP. Do not write tests for imagined behavior. Use `grill-me` skill to pressure-test requirements.**

---

## Stage 2 · Test Cases from PRD (Agent → Human)

- [ ] For each FR-N in PRD, at least one TC exists
- [ ] **Time/Performance number extraction (V4)**：Time commitments in PRD acceptance criteria (e.g. "within X seconds") must be extracted as normal TC even without separate NFR section (include timing method, second limit, timeout behavior)
- [ ] Test types: `normal` + `exception` + `boundary` + `reverse` + `stress`
  - **Type applicability rule (V4)**: stress/reverse can be N/A, but **must explicitly declare N/A reason in TC document summary table**, never silently skip
  - Pure client project (Mini Program / H5 standalone) → `stress: N/A — client has no concurrent scenario`
  - Single-user standalone feature → `reverse: N/A — no multi-user data isolation` (or recommend adding in next round)
- [ ] Test names follow: `TC-{ModuleName}-{SequenceNumber}-{Type}`
- [ ] Each TC has: Metadata (TC ID/FR/Type/Priority/Module/Created) + Preconditions + Test Steps + Expected Results + **Failure Handling** + **Code Template**
- [ ] **Scenario coverage report**: 100% of FRs mapped to at least one TC

**Sign-off gate**: Human reviews the TC list against the PRD before Stage 3 starts. If you can't trace a TC back to an FR, it's scope creep.

---

## Stage 3 · Implementation (Agent → Tests Pass)

**First determine the scenario (V4 decision tree)**:

```
Does business code exist?
├─ No → Green Field (new feature development, jump to "Green Field")
└─ Yes → Retro-test (existing business code, jump to "Retro-test")
```

### Green Field (Code Does Not Exist)

- [ ] **Vertical slicing**: one TC → minimal implementation → next TC
- [ ] Frameworks correct: JUnit 5 + Mockito (Spring) / miniprogram test / Jest+Testing Library (Web)
- [ ] Mocks only at system boundaries (external APIs, DB, time)
- [ ] No mocking of own classes / internal collaborators
- [ ] Tests pass after each red→green cycle
- [ ] **DO NOT do horizontal slicing** (all tests first, then all code)

### Retro-test (Existing Business Code, Read-Only Verification)

- [ ] **Do NOT modify existing code** to "make tests pass" (unless user explicitly requests fixing)
- [ ] Read source code / run tests for each TC, record results
- [ ] **Three-state judgment (V4, binary hard judgment is forbidden)**：
  | Result | Judgment Criteria |
  |--------|------------------|
  | **PASS** | Automated test passes; **or** pure logic TC confirmed by source code review that implementation matches Expected Results, and behavior **does not depend on real device rendering/timing** |
  | **FAIL** | Corresponding implementation not found, or implementation clearly contradicts Expected Results → also record defect |
  | **N/A (pending manual/real-device)** | Expected result depends on real device behavior: scroll positioning, animations, layout occlusion, performance timing, permission dialogs, recording/camera hardware, etc. |
- [ ] **Minimum evidence standard (V4)**: Each verification record's evidence must include at least one `file-relative-path:line-number`; "logic exists" does not constitute sufficient evidence
- [ ] **Forbidden: "code-layer PASS + note says pending real device"**: If any item in Expected Results depends on real device, judge the whole TC as N/A; in evidence note "logic verified, visual/animation pending real device"

**When a TC fails (Green Field)**:
- Bug in implementation → fix code, re-run TC
- TC is wrong (misunderstood PRD) → fix TC, re-run
- PRD is wrong → escalate to human, update PRD, regenerate affected TCs

---

## Stage 4 · Test Report (Agent → Human)

- [ ] **Test count by type** (including explicit N/A type declaration row)
- [ ] **Verification results**: passed / failed / **N/A (pending manual/real-device)**
- [ ] **Scenario coverage**: 100% of FRs have at least 1 TC
- [ ] **Code coverage (V4 honest rule)**：
  - Has coverage tool (JaCoCo / istanbul) → give number, target 80%+ critical paths
  - **No tool (e.g. Mini Program) → keep section, fill N/A + reason, do NOT delete section, do NOT fabricate percentage**
- [ ] **Defect list** (if any): ID / Severity / TC / Description / Status
- [ ] **Verification evidence**: each item includes 文件:行号
- [ ] **Open items / 待办**: TCs not verified on real device, modules without test framework, visual verification gaps
- [ ] **Red→green cycles** (Green Field) / N/A (Retro-test)

**Report status rules (V4)**：
- `PASS`: no FAIL, no open P0/P1, no pending critical N/A TCs
- `FAIL`: exists FAIL
- `INCOMPLETE`: exists N/A pending real device or open P1 — **do NOT label PASS just to look good**

**Sign-off gate**: Human reads the report. If satisfied → feature ships. If not → loop back to Stage 3.

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│   STAGE 1:  PRD          (human only)                   │
│            ↓                                             │
│   STAGE 2:  Test Cases   (agent generates, human review)│
│            ↓                                             │
│   STAGE 3:  Green Field  →  code → red→green            │
│            Retro-test   →  read code → PASS/FAIL/N/A    │
│            ↓                                             │
│   STAGE 4:  Report       (agent generates, human review)│
│            ↓                                             │
│       [PASS]       → ship                                │
│       [FAIL]       → back to Stage 3                    │
│       [INCOMPLETE] → real-device closure + re-judge      │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

**Time budget per stage** (suggested):
- Stage 1: 30-60 min (depends on PRD complexity)
- Stage 2: 15-30 min (agent generates, you review)
- Stage 3: 1-3 hours (Green Field) / 30-60 min (Retro-test source code review)
- Stage 4: 15 min (agent generates report)
