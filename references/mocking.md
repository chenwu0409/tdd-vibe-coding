# Mocking Guidelines (TDD-as-a-Skill)

> Based on the OpenClaw `tdd` skill's mocking.md. Adapted for Spring Boot + JUnit 5.

---

## Mock at System Boundaries Only

**DO mock**:
- External APIs (payment, email, SMS, third-party services)
- Databases (when you can't use a test DB)
- Time / randomness
- File system / network calls
- Anything outside your control

**DO NOT mock**:
- Your own classes or modules
- Internal collaborators
- Anything you can control
- Pure functions (they have no side effects to mock)

---

## Mocking in Spring Boot + JUnit 5

### Using `@MockBean` (Spring Test)

```java
@SpringBootTest
class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    
    @MockBean  // ← mock at system boundary
    private PaymentClient paymentClient;  // external API
    
    @MockBean  // ← mock at system boundary
    private EventPublisher eventPublisher;  // external system
    
    @Test
    void tcOrder001_paymentFailureReturnsError() {
        // Arrange
        when(paymentClient.charge(any(BigDecimal.class))))
            .thenThrow(new PaymentException("service down"));
        
        // Act
        OrderResult result = orderService.create(testOrder);
        
        // Assert
        assertThat(result.getStatus()).isEqualTo("failed");
        verify(eventPublisher, never()).publish(any());
    }
}
```

### Using `@Mock` (Plain Mockito, no Spring context)

```java
class OrderCalculatorTest {
    @Mock
    private taxService taxService;  // your own class — DON'T DO THIS
    
    // ... this is wrong
}
```

If you find yourself mocking your own classes, **stop and refactor**. The dependency exists for a reason — inject a real implementation in tests, or extract an interface.

### Test Database (Preferred over Mocking DB)

```java
@SpringBootTest
@ActiveProfiles("test")  // application-test.yml uses H2 in-memory
class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void tcRepo001_saveAndRetrieveOrder() {
        // Use real DB, not a mock
        Order order = TestData.createOrder();
        orderRepository.save(order);
        
        Optional<Order> found = orderRepository.findById(order.getId());
        assertThat(found).isPresent();
    }
}
```

---

## Design for Mockability

### Use Dependency Injection

```java
// GOOD: easy to mock
@Service
public class OrderService {
    private final PaymentClient paymentClient;
    
    public OrderService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;  // ← injected
    }
}

// BAD: hard to mock
@Service
public class OrderService {
    public void create(Order order) {
        PaymentClient client = new StripeClient(env.get("STRIPE_KEY"));
        // ↑ can't mock this without static mocking
    }
}
```

### Prefer SDK-style Interfaces

```java
// GOOD: each operation is independently mockable
public interface PaymentClient {
    Result charge(BigDecimal amount);
    Result refund(String chargeId);
    Status checkStatus(String chargeId);
}

// BAD: requires conditional logic in mock
public interface HttpClient {
    Response execute(Request request);  // too generic
}
```

---

## Time Mocking

```java
// Use Clock or TimeMachine
@Test
void tcOrder003_expiredOrderIsCancelled() {
    Clock fixedClock = Clock.fixed(
        Instant.parse("2026-10-09T15:00:00Z"), 
        ZoneOffset.UTC
    );
    OrderService service = new OrderService(paymentClient, fixedClock);
    
    // Test logic...
}
```

Avoid `Thread.sleep()` in tests — it's flaky and slow.

---

## Common Mocking Mistakes

| Mistake | Why it's wrong | Fix |
|---|---|---|
| Mocking `Repository` to return data | Tests your mock, not your code | Use test DB (H2 / Testcontainers) |
| Mocking your own service | Hides real bugs | Inject real implementation in tests |
| `when(x).thenReturn(y).thenReturn(z)` | Sequence mocks are brittle | Refactor to single-state mocks |
| Verifying call count without testing behavior | Tests implementation, not contract | Test the outcome instead |
| `Mockito.mockStatic(Class.class)` | Indicates bad design | Refactor to use DI |
