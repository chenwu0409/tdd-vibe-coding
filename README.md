# tdd-vibe-coding · Test-Driven Development for AI-Human Collaboration

> **Skill version**: v1.0 (2026-10-09)
> **Author**: 启明（@AI-Boss）+ 陈武
> **License**: 一堂 AI 大航海作业

---

## What is this?

A reusable **skill** that turns "AI writes code, human tests it" into "AI generates tests from PRD, then writes code to pass them." The goal: **90-95% of low-level bugs caught before human review**.

**Based on**:
- OpenClaw `tdd` skill (mattpocock 全套, red→green 循环)
- 一堂 AI 大航海作业 #1 (陈武, 2026-10-09)

**Adapted for**:
- Vibe Coding workflows (PRD → Test Cases → Code → Report)
- Multi-language projects (Spring Boot Java + 小程序 + Web)
- AI agent collaboration (not just human developers)

---

## When to use this skill

**Use when**:
- You're building a new feature and have a PRD/Spec
- You want the AI to generate test cases from the spec
- You want scenario coverage, not just code coverage
- You want automated red→green cycles, not "AI writes, human clicks"

**Don't use when**:
- You're exploring a prototype (use `prototype` skill instead)
- You have no PRD (use `grill-me` skill first to pressure-test requirements)
- You're refactoring existing code (use `code-review` skill)

---

## How to use

1. **Read** [SKILL.md](SKILL.md) — the 4-stage workflow
2. **Print** [references/workflow-checklist.md](references/workflow-checklist.md) — keep it on your desk
3. **Use** [references/prd-template.md](references/prd-template.md) for every new feature
4. **Generate** test cases with the agent (Stage 2)
5. **Implement** with the agent (Stage 3)
6. **Sign off** with [references/report-template.md](references/report-template.md) (Stage 4)

---

## File structure

```
tdd-skill/
├── SKILL.md                          ← 4-stage workflow (the entry point)
├── README.md                          ← this file
├── references/
│   ├── workflow-checklist.md          ← 1-page summary (print me!)
│   ├── prd-template.md                ← PRD template
│   ├── test-case-template.md          ← Test Case schema
│   ├── mocking.md                     ← Mocking guidelines (Spring + JUnit 5)
│   ├── anti-patterns.md               ← Top 5 mistakes to avoid
│   └── report-template.md             ← Test report schema
└── examples/
    └── ai-health-bpm/                ← Full worked example
        ├── 01-prd.md
        ├── 02-test-cases.md           (15 TCs, 100% FR coverage)
        ├── 03-implementation/         (Spring Boot + JUnit 5)
        │   ├── VitalSignsService.java
        │   └── VitalSignsServiceTest.java
        └── 04-test-report.md          (3 red→green cycles, 45 min)
```

---

## Key principles

1. **PRD first, tests second** — never write tests without a PRD
2. **Scenario coverage, not code coverage** — 100% of FRs covered, 80%+ on critical code
3. **Vertical slicing** — one test → one implementation → repeat
4. **Public interfaces only** — never test internals
5. **Independent expected values** — never let tests restate the implementation

See [references/anti-patterns.md](references/anti-patterns.md) for the full list.

---

## Compatibility

This skill is designed to work with:
- ✅ OpenClaw (tested 2026-10-09)
- ⏳ Harness (planned)
- ⏳ Codex (planned)
- ⏳ Trae match skill (planned)

---

## Version history

- **v1.0** (2026-10-09) — initial version
  - 4-stage workflow: PRD → Test Cases → Code → Report
  - 5 reference docs (checklist, prd, test-case, mocking, anti-patterns, report)
  - 1 worked example: ai-health BPM module (15 TCs, 100% FR coverage)
  - 3 red→green cycles, 45 min time to green

---

## Future work

- [ ] Add Harness/Codex/Trae compatibility
- [ ] More examples (multi-tenant, async, idempotent operations)
- [ ] Integration with JaCoCo (Java) / istanbul (JS) for code coverage
- [ ] CI/CD integration (auto-run tests on PR)
- [ ] Property-based testing (QuickTheories / fast-check)
