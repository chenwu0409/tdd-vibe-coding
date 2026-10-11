# Test Cases · Vital Signs Submission (AI Health BPM Module)

> Generated from `01-prd.md` using the tdd-vibe-coding skill.
> Scenario coverage: **100% of FRs covered** (4 FRs → 15 TCs).

---

## TC-Vital-001-normal: User can submit valid blood pressure reading

**Feature Requirement**: FR-1
**Preconditions**: User authenticated (user_id = `user-001`)
**Test Steps**:
1. POST /api/v1/vitals with `{type: "blood_pressure", value: {systolic: 145, diastolic: 92}}`
2. Verify response status
3. Verify response body contains ID and stored values
4. Verify DB has the record

**Expected Results**:
- Status: 201
- Body: `{id: uuid, user_id: user-001, type: blood_pressure, value: {145, 92}, recorded_at: ..., created_at: ...}`
- DB: 1 row in `vital_reading` table

---

## TC-Vital-002-normal: User can submit valid blood sugar reading

**Feature Requirement**: FR-1
**Preconditions**: User authenticated
**Test Steps**: Same as TC-001 but with `{type: "blood_sugar", value: 5.5}`
**Expected**: 201, stored value = 5.5

---

## TC-Vital-003-normal: User can submit valid weight reading

**Feature Requirement**: FR-1
**Preconditions**: User authenticated
**Test Steps**: Same as TC-001 but with `{type: "weight", value: 70.5}`
**Expected**: 201, stored value = 70.5

---

## TC-Vital-004-normal: User can submit valid heart rate reading

**Feature Requirement**: FR-1
**Preconditions**: User authenticated
**Test Steps**: Same as TC-001 but with `{type: "heart_rate", value: 75}`
**Expected**: 201, stored value = 75

---

## TC-Vital-005-normal: User can submit reading with custom recorded_at

**Feature Requirement**: FR-1
**Preconditions**: User authenticated
**Test Steps**: POST with explicit `recorded_at: "2026-10-08T08:00:00+08:00"`
**Expected**: 201, recorded_at matches the request

---

## TC-Vital-006-exception: 400 when type is invalid

**Feature Requirement**: FR-3
**Preconditions**: User authenticated
**Test Steps**: POST with `{type: "invalid_type", value: {}}`
**Expected**: 400, error message mentions invalid type

---

## TC-Vital-007-exception: 400 when blood pressure systolic out of range (high)

**Feature Requirement**: FR-3
**Preconditions**: User authenticated
**Test Steps**: POST with `{type: "blood_pressure", value: {systolic: 300, diastolic: 90}}`
**Expected**: 400, error message mentions systolic out of range

---

## TC-Vital-008-exception: 400 when blood pressure diastolic out of range (low)

**Feature Requirement**: FR-3
**Preconditions**: User authenticated
**Test Steps**: POST with `{type: "blood_pressure", value: {systolic: 120, diastolic: 20}}`
**Expected**: 400, error message mentions diastolic out of range

---

## TC-Vital-009-exception: 400 when JSON is malformed

**Feature Requirement**: FR-3
**Preconditions**: User authenticated
**Test Steps**: POST with `{type: "blood_pressure", value: {systolic: 145,` (unclosed)
**Expected**: 400, error message about JSON parsing

---

## TC-Vital-010-boundary: Blood pressure at lower bound (50/30)

**Feature Requirement**: FR-3
**Preconditions**: User authenticated
**Test Steps**: POST with `{type: "blood_pressure", value: {systolic: 50, diastolic: 30}}`
**Expected**: 201 (just inside valid range)

---

## TC-Vital-011-boundary: Blood pressure at upper bound (250/150)

**Feature Requirement**: FR-3
**Preconditions**: User authenticated
**Test Steps**: POST with `{type: "blood_pressure", value: {systolic: 250, diastolic: 150}}`
**Expected**: 201 (just inside valid range)

---

## TC-Vital-012-reverse: User cannot fetch another user's data

**Feature Requirement**: FR-4
**Preconditions**: user-001 authenticated
**Test Steps**:
1. POST a reading as user-001 (creates reading_id)
2. Authenticate as user-002
3. GET /api/v1/vitals/{reading_id}
**Expected**: 403 forbidden

---

## TC-Vital-013-reverse: Empty value object rejected

**Feature Requirement**: FR-3
**Preconditions**: User authenticated
**Test Steps**: POST with `{type: "blood_pressure", value: {}}`
**Expected**: 400, error message about missing fields

---

## TC-Vital-014-normal: User can retrieve their history

**Feature Requirement**: FR-2
**Preconditions**: User authenticated, 3 readings already submitted
**Test Steps**: GET /api/v1/vitals
**Expected**: 200, body has 3 items in desc order

---

## TC-Vital-015-stress: 100 concurrent submissions don't lose data

**Feature Requirement**: NFR-1
**Preconditions**: User authenticated
**Test Steps**:
- Use `ExecutorService` with 10 threads, 100 tasks
- Each task submits a unique reading
- Wait for all to complete
- Query DB: count should be 100
**Expected**: All 100 readings stored, no duplicates, no data loss

---

## Summary

| Type | Count | FRs covered |
|---|---|---|
| normal | 5 | FR-1, FR-2 |
| exception | 4 | FR-3 |
| boundary | 2 | FR-3 |
| reverse | 2 | FR-3, FR-4 |
| stress | 1 | NFR-1 |
| **Total** | **15** | **4/4 FRs covered (100%)** |
