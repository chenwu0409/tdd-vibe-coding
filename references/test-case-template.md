# Test Case Template (TDD-as-a-Skill)

> Every test case follows this structure. The agent generates these from the PRD.

---

## TC-{ModuleName}-{SequenceNumber}-{Type}

### Metadata

| Field | Value |
|---|---|
| **TC ID** | TC-{ModuleName}-{SequenceNumber}-{Type} |
| **Feature Requirement** | FR-{N} (link to PRD) |
| **Type** | `normal` / `exception` / `boundary` / `reverse` / `stress` |
| **Priority** | P0 (critical) / P1 (important) / P2 (nice-to-have) |
| **Module** | {ModuleName} |
| **Author** | {agent name + human reviewer} |
| **Created** | {date} |

### Specification

**Preconditions**:
- {State 1: user authenticated, cart exists, ...}
- {State 2: ...}
- {Test data: 3 orders, 1 user, ...}

**Test Steps**:
1. {Step 1: send POST /api/... with body X}
2. {Step 2: ...}
3. {Step 3: ...}

**Expected Results**:
- HTTP status: `{200/201/400/401/500}`
- Response body: `{...}`
- Side effects: {DB row created, event published, ...}

**Failure Handling**:
- {If status 500: check logs at /var/log/...}
- {If response missing field: ...}

---

### Code Template

#### Backend (JUnit 5 + Spring Boot)

##### Normal Test

```java
@Test
@DisplayName("TC-BPM-001-normal: User can create order with valid cart")
void tcBpm001_userCanCreateOrderWithValidCart() {
    // Given (preconditions)
    User testUser = TestDataFactory.createUser("user-001");
    Cart testCart = TestDataFactory.createCartWithItems(testUser, 3);

    // When (test steps)
    ResponseEntity<OrderResponse> response = orderController.create(
        testUser.getId(),
        new CreateOrderRequest(testCart.getId())
    );

    // Then (expected)
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getStatus()).isEqualTo("confirmed");
    assertThat(orderRepository.findById(response.getBody().getOrderId()))
        .isPresent()
        .get()
        .satisfies(order -> {
            assertThat(order.getUserId()).isEqualTo(testUser.getId());
            assertThat(order.getItems()).hasSize(3);
        });
}
```

##### Exception Test (API Error)

```java
@Test
@DisplayName("TC-BPM-002-exception: Returns 500 when payment service is down")
void tcBpm002_paymentServiceDown() {
    // Given (preconditions)
    User testUser = TestDataFactory.createUser("user-001");
    Cart testCart = TestDataFactory.createCartWithItems(testUser, 1);
    when(paymentClient.charge(any())).thenThrow(new PaymentException("Service unavailable"));

    // When (test steps)
    ResponseEntity<ErrorResponse> response = orderController.create(
        testUser.getId(),
        new CreateOrderRequest(testCart.getId())
    );

    // Then (expected)
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().getError()).isEqualTo("PAYMENT_SERVICE_ERROR");
    assertThat(orderRepository.findByUserId(testUser.getId())).isEmpty(); // No order created
}
```

##### Boundary Test (Empty/Null)

```java
@Test
@DisplayName("TC-BPM-003-boundary: Returns 400 when cart is empty")
void tcBpm003_emptyCart() {
    // Given (preconditions)
    User testUser = TestDataFactory.createUser("user-001");
    Cart emptyCart = TestDataFactory.createEmptyCart(testUser);

    // When (test steps)
    ResponseEntity<ErrorResponse> response = orderController.create(
        testUser.getId(),
        new CreateOrderRequest(emptyCart.getId())
    );

    // Then (expected)
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().getError()).isEqualTo("CART_EMPTY");
}
```

---

#### Frontend (WeChat Mini Program)

> **Note**: WeChat Mini Program doesn't have an official test framework like Jest. These templates are for reference. In practice, you may need to use manual verification or integration tests.

##### Normal Test (app.request)

```javascript
// TC-TEXT-001-normal: Text input → Confirm → Storage
test('TC-TEXT-001-normal: text input creates message and receives response', async () => {
  // Given (preconditions)
  const mockResponse = {
    items: [{
      category: 'vital',
      type: 2,
      value: 6.8,
      period: 'fasting'
    }]
  };
  jest.spyOn(app, 'request').mockResolvedValue(mockResponse);

  // When (test steps)
  const inputText = '空腹血糖 6.8';
  const response = await app.request({
    url: '/v1/diet/recognize',
    method: 'POST',
    data: { rawText: inputText }
  });

  // Then (expected)
  expect(response.items).toHaveLength(1);
  expect(response.items[0].value).toBe(6.8);
  expect(response.items[0].period).toBe('fasting');
});
```

##### Exception Test (Network Error)

```javascript
// TC-TEXT-002-exception: Returns error when network fails
test('TC-TEXT-002-exception: handles network error gracefully', async () => {
  // Given (preconditions)
  jest.spyOn(app, 'request').mockRejectedValue(new Error('Network timeout'));

  // When (test steps)
  try {
    await app.request({
      url: '/v1/diet/recognize',
      method: 'POST',
      data: { rawText: '空腹血糖 6.8' }
    });
  } catch (error) {
    // Then (expected)
    expect(error.message).toContain('timeout');
    // Error handling should show user-friendly message
  }
});
```

##### Boundary Test (Empty/Long Input)

```javascript
// TC-TEXT-003-boundary: Handles empty input gracefully
test('TC-TEXT-003-boundary: rejects empty input', async () => {
  // Given (preconditions)
  const inputText = '   '; // whitespace only

  // When (test steps) - input validation happens client-side
  const isValid = inputText.trim().length > 0;

  // Then (expected)
  expect(isValid).toBe(false);
  // Should prevent sending empty message
});
```

##### Mock Example (app.request)

```javascript
// Mock the entire app.request for different scenarios
describe('Voice Recognition Flow', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('TC-VOICE-001-normal: voice upload → ASR → parse', async () => {
    // Mock COS upload
    jest.spyOn(app, 'uploadFile').mockResolvedValue('https://cos.example.com/voice.m4a');

    // Mock ASR
    jest.spyOn(app, 'request')
      .mockResolvedValueOnce({ success: true, text: '空腹血糖 6.8' })  // ASR response
      .mockResolvedValueOnce({ items: [{ category: 'vital', value: 6.8 }] }); // Parse response

    // When (test steps)
    const asrResult = await app.request({ url: '/v1/asr/recognize', ... });
    const parseResult = await app.request({ url: '/v1/diet/recognize', ... });

    // Then (expected)
    expect(asrResult.text).toBe('空腹血糖 6.8');
    expect(parseResult.items).toHaveLength(1);
  });
});
```

##### Page Lifecycle Test

```javascript
// TC-MULTI-001-normal: Page loads correctly
test('TC-MULTI-001-normal: page lifecycle works', async () => {
  // Given (preconditions)
  const pageInstance = getPageInstance('pages/multimodal-input/index');

  // When (test steps) - onLoad lifecycle
  await pageInstance.onLoad();

  // Then (expected)
  expect(pageInstance.data.messages).toBeDefined();
  expect(pageInstance.data.sessionId).toBeDefined();
  expect(pageInstance.data.currentTimeStr).toBeTruthy(); // Not empty
});
```

##### Component Interaction Test

```javascript
// TC-GUIDE-001-normal: Onboarding guide interaction
test('TC-GUIDE-001-normal: onboarding step progression', async () => {
  // Given (preconditions)
  const pageInstance = getPageInstance('pages/multimodal-input/index');
  pageInstance.setData({ showOnboarding: true, onboardingStep: 1 });

  // When (test steps) - next step
  await pageInstance.nextOnboardingStep();

  // Then (expected)
  expect(pageInstance.data.onboardingStep).toBe(2);

  // When (test steps) - skip
  await pageInstance.skipOnboarding();

  // Then (expected)
  expect(pageInstance.data.showOnboarding).toBe(false);
});
```

---

### Anti-patterns to Avoid

See [anti-patterns.md](anti-patterns.md). In short:
- **Don't** mock internal collaborators
- **Don't** use implementation-derived expected values (no `expected = items.reduce(...)`)
- **Don't** test private methods
- **Don't** verify through DB queries when an interface exists

---

## Code Template Summary

| Test Type | Backend (Java) | Frontend (Mini Program) |
|-----------|---------------|------------------------|
| **normal** | JUnit 5 controller test | Mock `app.request`, verify response |
| **exception** | Mock service to throw, verify error response | Mock `app.request` rejection, verify error handling |
| **boundary** | Test empty/null/zero values | Test input validation |
| **reverse** | Test authorization/permission errors | Test user cannot access others' data |
| **stress** | Test concurrent requests | N/A — 纯客户端项目无并发场景，当前开发环境不具备压测条件；时效要求已作为功能性 normal TC 独立覆盖（TC-PERF-001/002/003），不并入 stress |

## Mini Program Testing Considerations

1. **No official test framework**: WeChat Mini Program doesn't have a widely adopted unit testing framework. Consider:
   - Manual testing for UI/UX
   - Integration testing via real device
   - Mock `app.request` for API testing

2. **Common patterns to test**:
   - Page lifecycle (`onLoad`, `onShow`, `onHide`)
   - Component state changes
   - Input validation
   - Error handling

3. **Mock strategy**:
   ```javascript
   // Mock app.request for API testing
   jest.spyOn(app, 'request').mockResolvedValue(mockData);

   // Mock app.uploadFile for COS upload
   jest.spyOn(app, 'uploadFile').mockResolvedValue('https://cos.url/file');
   ```
