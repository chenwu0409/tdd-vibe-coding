# Anti-patterns (TDD-as-a-Skill)

> These are the 5 most common mistakes when applying TDD to Vibe Coding. Each has a "tell" and a "fix".

---

## 1. Tests without PRD

**What it looks like**:
> Human: "Write me some tests for the new feature."
> Agent: *generates 50 tests for a feature no one has specified*

**Why it's wrong**:
- The agent imagines behavior, not verifies it
- Tests may pass but verify the wrong thing
- Human discovers the bug in production, not in CI

**The tell**:
- Test names use words not in the PRD
- Test count exceeds the number of FRs
- You can't trace a test back to a specific FR

**The fix**:
- **Stage 1 is non-negotiable.** PRD must exist and be signed off before any test is written.
- If the PRD is vague, use `grill-me` skill to pressure-test requirements.
- Every test must have `**Feature Requirement**: FR-{N}` linking back to the PRD.

---

## 2. Implementation-coupled tests

**What it looks like**:
```java
// BAD: tests implementation, not behavior
@Test
void tcOrder001_checkoutCallsPaymentService() {
    when(paymentService.charge(any())).thenReturn(success);
    checkoutService.checkout(cart);
    verify(paymentService, charge(100.0));  // ← tests call, not behavior
}
```

**Why it's wrong**:
- Refactor breaks the test even if behavior is unchanged
- Test name describes HOW, not WHAT
- Future maintainers can't understand what capability is being tested

**The tell**:
- Test name has "calls", "invokes", "uses", "sends to"
- `verify(mock).method(...)` appears in the test
- Test fails when you refactor without changing behavior

**The fix**:
- Test through public interfaces only
- Test the outcome (status, return value, side effect observable through interface)
- Use [mocking.md](mocking.md) to decide what to mock

---

## 3. Tautological tests

**What it looks like**:
```java
// BAD: expected value restates the implementation
@Test
void testAdd() {
    int a = 2, b = 3;
    int expected = a + b;  // ← same as the code!
    assertEquals(expected, calculator.add(a, b));  // always passes
}
```

**Why it's wrong**:
- Test passes by construction
- Cannot catch bugs
- Provides false confidence

**The tell**:
- Expected value is computed in the test using the same logic as the code
- Test uses a snapshot derived "by hand" the same way
- Test asserts `constant == constant`

**The fix**:
- Expected values must come from an **independent source of truth**:
  - A known-good literal (`assertEquals(15, calculateTotal(...))`)
  - A worked example from the spec
  - A property-based test (e.g., `commutative property: add(a,b) == add(b,a)`)
- The PRD is your independent source of truth. Read it for expected values.

---

## 4. Horizontal slicing (all tests first, then all code)

**What it looks like**:
```
Day 1: Agent writes 30 test cases
Day 2: Agent writes 1000 lines of code
Day 3: "Why are 15 tests failing?"
```

**Why it's wrong**:
- Tests verify _imagined_ behavior, not _evolved_ behavior
- Each red→green cycle teaches you something; horizontal slicing throws that away
- You commit to test structure before understanding the implementation
- Fixing a failing test often requires changing the test itself (because it was imagined)

**The tell**:
- More than 1 commit per day is "all tests"
- Tests are written before any implementation
- Implementation changes cause > 30% of tests to need rewriting

**The fix**:
- **Vertical slicing**: one test → one minimal implementation → next test
- Each cycle is a **tracer bullet** that responds to what the last cycle taught you
- Stage 3 should be many short red→green cycles, not one long "implement everything" cycle

---

## 5. 100% code coverage as goal

**What it looks like**:
> "We need 100% line coverage before we ship."

**Why it's wrong**:
- Code coverage is a **means**, not an end
- 100% line coverage can be achieved with bad tests (e.g., tautological ones)
- Forces the agent to write tests for trivial code (getters, setters, DTOs)
- Diverts attention from what matters: **scenario coverage**

**The tell**:
- PR review comments focus on coverage percentage
- Test count grows but defects don't decrease
- Team is proud of 95% coverage but production still has bugs

**The fix**:
- **Scenario coverage is the real goal**: 100% of PRD's FRs are covered by at least one TC
- **Code coverage is a sanity check**: ≥ 80% on critical paths (business logic, not DTOs)
- Use the [report-template.md](report-template.md) to surface both

---

## Quick Reference

| Anti-pattern | Tell | Fix |
|---|---|---|
| Tests without PRD | Words not in PRD | PRD first, then tests |
| Implementation-coupled | `verify(mock).method()` | Test through interface |
| Tautological | `expected = a + b` | Use known literal / spec value |
| Horizontal slicing | All tests day 1, all code day 2 | Vertical: 1 test → 1 impl → repeat |
| 100% code coverage goal | Coverage wars | Scenario coverage (FR → TC) is real goal |
