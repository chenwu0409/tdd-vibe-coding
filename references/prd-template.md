# PRD Template (TDD-as-a-Skill)

> Use this template for every feature. Without a PRD, you cannot start Stage 2.

---

## Feature Name

**Name**: 
**Author**: 
**Date**: 
**Status**: Draft / In Review / Approved

---

## Background

Why are we building this? What problem does it solve? (2-3 sentences)

---

## Functional Requirements

Each FR should be **atomic** and **testable**. Number them for easy reference.

### FR-1: {Title}

**Description**: {What the system does}
**Acceptance Criteria**:
- {Criterion 1: observable, testable}
- {Criterion 2}
**API Contract** (if any):
- `POST /api/v1/{endpoint}` — {request body, response, status codes}
**Data Model** (if any):
- `{Entity}` — fields: {field1, field2, field3}

### FR-2: {Title}

...

### FR-3: {Title}

...

---

## Non-Functional Requirements

### NFR-1: Performance

- Response time p99 ≤ {X}ms
- Throughput ≥ {Y} requests/sec

### NFR-2: Security

- Authentication: {method}
- Authorization: {role-based / ACL / ...}
- Data validation: {rules}

### NFR-3: Reliability

- Error handling: {strategy}
- Retry policy: {rules}
- Timeout: {values}

### NFR-4: Observability

- Logging: {what to log}
- Metrics: {which metrics}
- Tracing: {trace ID propagation}

---

## API Contracts

### Endpoint 1

```
POST /api/v1/orders
```

**Request body**:
```json
{
  "user_id": "string",
  "items": [
    { "product_id": "string", "quantity": "integer" }
  ]
}
```

**Response 200**:
```json
{
  "order_id": "string",
  "status": "confirmed",
  "total": "number"
}
```

**Error responses**:
- `400` — invalid input
- `401` — unauthorized
- `409` — conflict (e.g., out of stock)
- `500` — internal error

### Endpoint 2

...

---

## Data Model

### Entity: Order

| Field | Type | Required | Description |
|---|---|---|---|
| id | UUID | Yes | Primary key |
| user_id | UUID | Yes | Foreign key to User |
| status | enum | Yes | pending / confirmed / shipped / cancelled |
| total | decimal | Yes | Total amount |
| created_at | timestamp | Yes | Creation time |
| updated_at | timestamp | Yes | Last update time |

### Entity: OrderItem

...

---

## Acceptance Criteria (Summary)

A feature is "done" when:
- [ ] All FR-1, FR-2, FR-3 ... pass their test cases
- [ ] All NFR-1, NFR-2, ... are met
- [ ] Code coverage ≥ 80% on critical paths
- [ ] No P0/P1 defects open
- [ ] Test report signed off

---

## Out of Scope

- {Feature X} — will be addressed in a separate PRD
- {Feature Y}

---

## Open Questions

- {Question 1}
- {Question 2}
