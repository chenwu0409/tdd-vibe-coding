# PRD · Vital Signs Submission (AI Health BPM Module)

**Name**: Vital Signs Submission API
**Author**: 陈武
**Date**: 2026-10-09
**Status**: Approved

---

## Background

The ai-health app lets users submit health readings (blood pressure, blood sugar, weight) via the multi-modal input page. Currently, the system accepts text/voice input and extracts vital signs using LLM. We need a backend API to validate and store these readings, then return a confirmation to the user.

This is a foundational module — once it works, we can build analytics, alerts, and dashboard on top.

---

## Functional Requirements

### FR-1: User can submit a vital signs reading

**Description**: User submits a reading via `POST /api/v1/vitals` with the reading type and value(s).

**Acceptance Criteria**:
- API accepts JSON body with `type` (enum) and `value` (object)
- API validates the value matches the type's schema
- API stores the reading with user_id, timestamp, type, value
- API returns 201 with the stored reading's ID

**API Contract**:
```
POST /api/v1/vitals

Request:
{
  "type": "blood_pressure",  // enum: blood_pressure, blood_sugar, weight, heart_rate
  "value": { "systolic": 145, "diastolic": 92 }  // schema depends on type
  "recorded_at": "2026-10-09T14:00:00+08:00"  // ISO 8601, optional (default: now)
}

Response 201:
{
  "id": "uuid",
  "user_id": "uuid",
  "type": "blood_pressure",
  "value": { "systolic": 145, "diastolic": 92 },
  "recorded_at": "2026-10-09T14:00:00+08:00",
  "created_at": "2026-10-09T14:05:00+08:00"
}

Errors:
- 400: invalid type, value out of range, malformed JSON
- 401: unauthorized
- 500: internal error
```

**Data Model**:
- `vital_reading` — id (UUID), user_id (UUID), type (enum), value (JSONB), recorded_at (timestamp), created_at (timestamp)

---

### FR-2: User can retrieve their own vital signs history

**Description**: User can fetch a paginated list of their past readings.

**Acceptance Criteria**:
- `GET /api/v1/vitals?type=&from=&to=&page=&size=` returns the user's readings (filtered)
- Default page size 20, max 100
- Sorted by `recorded_at` desc

**API Contract**:
```
GET /api/v1/vitals?type=blood_pressure&from=2026-10-01&to=2026-10-09&page=1&size=20

Response 200:
{
  "items": [ { "id": ..., "type": ..., "value": ..., "recorded_at": ... }, ... ],
  "total": 42,
  "page": 1,
  "size": 20
}
```

---

### FR-3: System validates value ranges

**Description**: Reject out-of-range values to prevent garbage data.

**Acceptance Criteria**:
- blood_pressure: systolic 50-250, diastolic 30-150
- blood_sugar: 1.0-30.0 (mmol/L)
- weight: 20.0-300.0 (kg)
- heart_rate: 30-220 (bpm)
- Out-of-range → 400 with error message

---

### FR-4: User cannot access another user's readings

**Description**: Authorization check on GET endpoint.

**Acceptance Criteria**:
- user_id filter is automatically applied (from JWT)
- Attempting to fetch another user's data → 403

---

## Non-Functional Requirements

### NFR-1: Performance

- POST /vitals: p99 ≤ 200ms
- GET /vitals: p99 ≤ 500ms (for 100 records)

### NFR-2: Security

- All endpoints require JWT authentication
- User can only access their own data
- Input validation at controller layer

### NFR-3: Reliability

- All errors logged with request_id
- Database transaction wraps the create operation

---

## Acceptance Criteria (Summary)

- [ ] All FR-1, FR-2, FR-3, FR-4 pass their test cases
- [ ] Scenario coverage = 100%
- [ ] Code coverage ≥ 80% on vital package
- [ ] No P0/P1 defects open
- [ ] Test report signed off

---

## Out of Scope (Future PRDs)

- Editing/deleting a reading
- Bulk import
- Sharing with caregiver / family member
- Alerts (e.g., "blood pressure too high")
- Analytics / dashboard / reports
