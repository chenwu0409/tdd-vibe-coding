# TDD Skill · Example: AI Health BPM Module

> This is a worked example showing how the tdd-vibe-coding skill applies to a real feature in the ai-health project.

---

## What this example demonstrates

A simple **BPM (Business Process Management) module** for the ai-health app:
- A user can submit a vital signs reading (e.g., blood pressure)
- The system validates it
- The system stores it
- The system returns a confirmation

This is a **complete PRD → Test Cases → Implementation → Report** walkthrough.

---

## Files in this example

- `01-prd.md` — the PRD for the BPM module (Stage 1)
- `02-test-cases.md` — Test Cases generated from the PRD (Stage 2)
- `03-implementation/` — the actual code (Stage 3)
  - `VitalSignsService.java`
  - `VitalSignsServiceTest.java`
- `04-test-report.md` — the final report (Stage 4)

---

## How to read this example

1. Read `01-prd.md` to understand what we're building
2. Read `02-test-cases.md` to see how the agent translated PRD → TCs
3. Read `03-implementation/` to see the actual Spring Boot code
4. Read `04-test-report.md` to see the final outcome

**Do not** copy-paste this example into your own project. The point is to see the **4-stage pattern** in action, then apply it to your own feature.
