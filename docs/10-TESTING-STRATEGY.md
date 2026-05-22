# Testing Strategy

This project follows a business-focused testing strategy.

The goal is not to maximize coverage numbers or create tests for every class. The goal is to protect the business behavior of the system with a small, valuable, fast, and maintainable test suite.

> We do not write tests to increase coverage.  
> We write tests to protect business behavior.

---

## 1. Core Testing Principle

Business logic must live in:

- Domain entities
- Value objects
- Domain services
- Application use cases

Business logic must not live in:

- Controllers
- API Gateway adapters
- Lambda handlers
- Repositories
- DynamoDB adapters
- JSON/API mappers
- Spring configuration classes
- Infrastructure classes

If a class is hard to test without AWS, Spring, DynamoDB, API Gateway, or the Lambda runtime, it is probably in the wrong layer.

The test suite should protect the business behavior of the application, not the framework wiring.

---

## 2. Testing Pyramid

The project should follow this practical testing pyramid:

```text
70% domain and use case unit tests
20% acceptance-style business flow tests
10% integration / contract / smoke tests
```

This is a guideline, not a strict metric.

The intention is:

```text
Many fast tests for business rules.
Some tests for complete business flows.
Few tests for infrastructure boundaries.
```

Avoid creating a large number of tests for framework glue code.

---

## 3. Unit Tests

Every relevant business rule must have unit tests.

Unit tests should focus mainly on:

- Domain entities
- Value objects
- Domain services
- Application use cases

For every relevant business rule, the minimum expected coverage is:

```text
1. Successful case
2. Invalid case
3. Boundary case
```

In Spanish, the rule is:

```text
Pasa
No pasa
Condición de borde
```

Examples:

```text
Successful case:
A valid order can be created.

Invalid case:
An order cannot be created with an empty item list.

Boundary case:
An order can be created when the requested quantity is exactly equal to the available stock.
```

Unit tests must be:

- Fast
- Deterministic
- Independent from external systems
- Easy to understand
- Focused on behavior, not implementation details

Unit tests must not require:

- AWS
- DynamoDB
- API Gateway
- Lambda runtime
- Spring application context
- Network access
- Real databases

---

## 4. Domain Unit Tests

Domain tests protect the core business rules.

They should test classes such as:

```text
Order
OrderItem
TableAccount
PaymentSplit
CatalogVersion
StockReservation
Branch
Tenant
Product
PriceList
Menu
KitchenOrder
```

Example business rules:

```text
A table can add products to an open account.
A table account cannot be closed if there is a pending amount.
A customer can pay only part of the account.
A partial payment reduces the remaining total.
A product cannot be ordered if there is no stock.
A product can be ordered when the requested quantity is exactly equal to the available stock.
A kiosk is treated as a special type of table.
A catalog version can be activated only if it is valid.
A kitchen order can be marked as ready only if it was previously confirmed.
```

For each rule, tests should include:

```text
1. Happy path / successful case
2. Rejection / invalid case
3. Boundary condition
```

Example:

```java
class StockReservationTest {

    @Test
    void shouldReserveStockWhenQuantityIsAvailable() {
        // successful case
    }

    @Test
    void shouldRejectReservationWhenQuantityIsGreaterThanAvailableStock() {
        // invalid case
    }

    @Test
    void shouldReserveStockWhenRequestedQuantityIsExactlyTheAvailableStock() {
        // boundary case
    }
}
```

Domain tests should use real domain objects.

Avoid mocking domain entities, value objects, or domain services.

---

## 5. Use Case Tests

Use cases are application-level orchestrators.

They coordinate domain objects and ports.

Use cases may contain orchestration logic, but they should not contain complex business rules that belong in the domain.

Use case tests should verify that a complete application action behaves correctly.

Examples:

```text
PlaceOrderUseCase
CancelOrderUseCase
ClosePartialAccountUseCase
CloseFullAccountUseCase
MarkOrderReadyUseCase
ReserveStockUseCase
ReleaseStockUseCase
UpdateCatalogVersionUseCase
ActivateCatalogVersionUseCase
CreateBranchUseCase
CreateTableUseCase
```

Use case tests should verify things such as:

```text
The correct domain operation is executed.
The correct repository is used through a port.
The expected domain event is published.
The expected result is returned.
Invalid input is rejected.
Boundary cases are handled correctly.
```

Use case tests may use mocks or in-memory fakes for ports.

Preferred test doubles:

```text
InMemoryOrderRepository
InMemoryTableRepository
InMemoryStockRepository
InMemoryCatalogRepository
InMemoryBranchRepository
InMemoryTenantRepository
FakeDomainEventPublisher
FakeClock
FakeIdGenerator
```

Example:

```java
@Test
void shouldPlaceOrderWhenProductsAreAvailable() {
    // given
    var table = givenOpenTable();
    var product = givenAvailableProduct("Coca-Cola", 10);

    // when
    var result = placeOrderUseCase.execute(
        new PlaceOrderCommand(
            table.id(),
            List.of(new OrderItemRequest(product.id(), 1))
        )
    );

    // then
    assertThat(result.orderId()).isNotNull();
    assertThat(orderRepository.findById(result.orderId())).isPresent();
    assertThat(stockRepository.findByProductId(product.id()).availableQuantity()).isEqualTo(9);
    assertThat(eventPublisher.publishedEvents()).hasSize(1);
}
```

Use case tests should verify behavior, not private implementation details.

Do not test private methods.

Do not test the internal sequence of method calls unless that sequence is part of the business contract.

---

## 6. Acceptance-Style Tests

Acceptance-style tests represent complete business scenarios.

They are written from the perspective of the business flow, not from the perspective of a single class or method.

Acceptance-style tests can be implemented directly with JUnit.

Cucumber/Gherkin is not required for this project.

For this project, prefer:

```text
JUnit + expressive test names + business-oriented given/when/then helpers
```

Gherkin should only be introduced if business users, QA, or product owners need to read and maintain executable specifications.

An acceptance-style test is valid even if it is written in JUnit, as long as the test represents a complete business scenario.

Example business scenario:

```text
The user orders a Coca-Cola.
The user closes only their part of the account.
That paid part is deducted from the table total.
The remaining items stay pending.
```

This type of scenario is an acceptance test because it describes business behavior end-to-end inside the application core.

---

## 7. Acceptance Test Package Structure

Acceptance-style tests must be organized in dedicated packages by business capability.

Recommended structure:

```text
src/test/java
  acceptance/
    ordering/
      PlaceOrderAcceptanceTest
      CancelOrderAcceptanceTest
      MarkOrderReadyAcceptanceTest

    account/
      ClosePartialAccountAcceptanceTest
      CloseFullAccountAcceptanceTest
      SplitPaymentAcceptanceTest

    catalog/
      UpdateCatalogVersionAcceptanceTest
      ActivateCatalogVersionAcceptanceTest

    stock/
      ReserveStockAcceptanceTest
      ReleaseStockAcceptanceTest

    branch/
      CreateBranchAcceptanceTest
      ConfigureBranchTablesAcceptanceTest

    kiosk/
      KioskPlaceOrderAcceptanceTest
```

The package should express the business area.

The test class should express the business flow.

---

## 8. Acceptance Test Style

Acceptance-style tests should use:

- Real use cases
- Real domain objects
- In-memory repositories
- Fake event publishers
- Fake clocks
- Fake ID generators

Acceptance-style tests should not use:

- API Gateway
- Lambda runtime
- DynamoDB
- AWS services
- Real network calls
- Real databases
- Spring context, unless strictly necessary

The goal is to test the business flow end-to-end inside the application core.

Acceptance tests should not duplicate controller tests, repository tests, or JSON mapper tests.

---

## 9. Acceptance Test Example: Partial Account Payment

Business scenario:

```text
A customer orders a Coca-Cola and a burger.
The customer pays only the Coca-Cola.
The Coca-Cola is closed as paid.
The burger remains pending.
The table total is updated correctly.
```

JUnit example:

```java
@Test
void customerPaysPartialAccountAndRemainingTableTotalIsUpdated() {
    // given
    var table = givenOpenTable("table-001");

    var cocaCola = givenProduct("product-001", "Coca-Cola", money("5.00"));
    var burger = givenProduct("product-002", "Burger", money("20.00"));

    var order = placeOrderUseCase.execute(
        new PlaceOrderCommand(
            table.id(),
            List.of(
                new OrderItemRequest(cocaCola.id(), 1),
                new OrderItemRequest(burger.id(), 1)
            )
        )
    );

    // when
    var result = closePartialAccountUseCase.execute(
        new ClosePartialAccountCommand(
            table.id(),
            List.of(order.itemIdFor(cocaCola.id()))
        )
    );

    // then
    assertThat(result.paidAmount()).isEqualTo(money("5.00"));
    assertThat(result.remainingAmount()).isEqualTo(money("20.00"));
    assertThat(result.closedItems()).contains(order.itemIdFor(cocaCola.id()));
    assertThat(result.pendingItems()).contains(order.itemIdFor(burger.id()));
}
```

This is an acceptance-style test even though it is written in JUnit.

The purpose is not to test a controller, endpoint, repository, or JSON mapping.

The purpose is to verify that a complete business behavior works correctly.

---

## 10. Acceptance Test Example: Kiosk Order To Kitchen

Business scenario:

```text
A kiosk is configured as a special table.
A customer places an order from the kiosk.
The order is created.
The kitchen receives the order.
The stock is reduced.
```

JUnit example:

```java
@Test
void kioskPlacesOrderAndKitchenReceivesIt() {
    // given
    var branch = givenBranch("branch-001");
    var kiosk = givenKioskAsSpecialTable(branch);
    var product = givenAvailableProduct("Coca-Cola", 10);

    // when
    var order = whenKioskPlacesOrder(kiosk, product, 1);

    // then
    thenOrderShouldBeCreated(order);
    thenKitchenShouldReceiveOrder(order);
    thenStockShouldBeReduced(product, 9);
}
```

---

## 11. Acceptance Test Naming

Acceptance test names should describe business behavior.

Prefer:

```java
customerPaysPartialAccountAndRemainingTableTotalIsUpdated()
kioskPlacesOrderAndKitchenReceivesIt()
tableOrderIsMarkedReadyByKitchen()
branchActivatesNewCatalogVersion()
orderIsRejectedWhenProductHasNoStock()
```

Avoid generic names like:

```java
testExecute()
testUseCase()
testHandler()
testController()
```

---

## 12. Given / When / Then Helpers

Acceptance tests may use helper methods to make the scenario readable.

Example:

```java
@Test
void kioskPlacesOrderAndKitchenReceivesIt() {
    // given
    var branch = givenBranch("branch-001");
    var kiosk = givenKioskAsSpecialTable(branch);
    var product = givenAvailableProduct("Coca-Cola", 10);

    // when
    var order = whenKioskPlacesOrder(kiosk, product, 1);

    // then
    thenOrderShouldBeCreated(order);
    thenKitchenShouldReceiveOrder(order);
    thenStockShouldBeReduced(product, 9);
}
```

Helper methods should express business language.

Good helper names:

```text
givenOpenTable
givenKioskAsSpecialTable
givenAvailableProduct
givenActiveCatalog
whenCustomerPlacesOrder
whenCustomerPaysPartialAccount
thenRemainingAmountShouldBe
thenKitchenShouldReceiveOrder
thenStockShouldBeReduced
```

Bad helper names:

```text
mockRepository
callExecute
createDto
assertResponse
```

---

## 13. Gherkin Alternative

Gherkin is optional.

The previous acceptance test could be expressed like this:

```gherkin
Feature: Partial account payment

  Scenario: Customer pays only part of a table account
    Given table "table-001" has an open account
    And the customer ordered 1 "Coca-Cola" with price 5.00
    And the customer ordered 1 "Burger" with price 20.00
    When the customer pays only the "Coca-Cola"
    Then the paid amount should be 5.00
    And the remaining amount should be 20.00
    And the "Coca-Cola" item should be closed as paid
    And the "Burger" item should remain pending
```

However, unless there is a real need for non-technical people to read or maintain these scenarios, prefer JUnit acceptance-style tests.

Reasons to avoid Gherkin at this stage:

```text
It adds feature files.
It adds step definitions.
It creates mapping between text and code.
It increases maintenance cost.
It can slow down development if the team is small.
```

Reasons to use Gherkin later:

```text
QA writes scenarios.
Product owners review scenarios.
Business users need readable executable documentation.
The team wants living documentation as part of the delivery process.
```

---

## 14. What Not To Unit Test By Default

Do not create unit tests by default for:

```text
Controllers
API Gateway adapters
Lambda handlers
Spring configuration
Simple JSON mappers
Simple DTOs
DynamoDB repositories
CloudWatch/X-Ray/logging configuration
SAM/OpenAPI infrastructure glue
```

These classes should be thin.

If one of these classes requires many tests, it probably contains logic that should be moved to the domain or application layer.

---

## 15. Repository Tests

Repositories should not be unit tested with mocks just to verify that a method was called.

Repository implementations are infrastructure details.

Only write repository integration tests when there is real value, such as:

```text
Complex DynamoDB key design
Non-trivial query behavior
Conditional writes
Optimistic locking
Pagination
Tenant isolation
Serialization/deserialization risk
```

These tests should be few and focused.

Examples:

```text
Verify that a tenant cannot read another tenant's items.
Verify that a conditional write prevents overwriting a newer catalog version.
Verify that a DynamoDB query returns the correct items for a branch and catalog version.
Verify that pagination works correctly for large result sets.
```

Do not duplicate business rule tests in repository tests.

---

## 16. Controller / API / Lambda Handler Tests

Controllers, API Gateway adapters, and Lambda handlers should have minimal tests.

Only add smoke or contract tests to verify:

```text
A valid request returns the expected HTTP status.
Invalid input returns 400.
Unexpected errors return 500.
The JSON:API envelope is respected.
The route is wired correctly.
```

Do not duplicate all domain, use case, and acceptance scenarios at the controller level.

Business scenarios belong in:

```text
Domain tests
Use case tests
Acceptance-style tests
```

Controller/API tests should only protect the transport boundary.

---

## 17. Integration / Smoke Tests

Integration tests should be few and focused.

They should be used only when the risk is in the integration with an external or infrastructure component.

Examples:

```text
DynamoDB adapter mapping
OpenAPI route wiring
API Gateway event mapping
JSON:API serialization/deserialization
CloudWatch/X-Ray configuration smoke checks
```

Integration tests should not duplicate business rule testing.

If a business rule is already tested in domain or use case tests, do not repeat it in an integration test.

---

## 18. Contract Tests

Contract tests are useful when the system exposes APIs or consumes APIs.

Use contract tests to verify:

```text
The request shape is valid.
The response shape is valid.
The JSON:API envelope is respected.
Required fields are present.
Error responses follow the expected format.
```

Contract tests should not re-test business logic.

Example:

```text
POST /orders with invalid input returns 400 with a JSON:API error response.
GET /price-lists/{priceListId} returns a JSON:API resource.
```

---

## 19. Test Data Builders

Prefer test data builders or factory methods to reduce duplication.

Examples:

```java
var table = aTable().open().build();
var product = aProduct()
    .named("Coca-Cola")
    .withStock(10)
    .withPrice("5.00")
    .build();
var order = anOrder()
    .forTable(table.id())
    .withItem(product.id(), 1)
    .build();
```

Builders should make tests easier to read.

Avoid large object creation blocks inside each test.

---

## 20. Naming Convention

Test names should describe business behavior.

Prefer:

```java
shouldClosePartialAccountWhenCustomerPaysSomeItems()
shouldRejectOrderWhenProductHasNoStock()
shouldActivateCatalogVersionWhenAllProductsAreValid()
shouldKeepTableOpenWhenThereArePendingItems()
shouldReserveStockWhenRequestedQuantityEqualsAvailableQuantity()
```

Avoid:

```java
testExecute()
testHandler()
testRepository()
testMapper()
testCreate()
```

---

## 21. Cursor / AI Coding Rules For Tests

When generating tests, Cursor or any AI assistant must follow these rules:

```text
Do not create tests just to increase coverage.
Do not create tests for every class by default.
Do not create unit tests for controllers, handlers, repositories, or configuration unless they contain real logic.
Prefer domain and use case tests.
For every relevant business rule, include at least: successful case, invalid case, and boundary case.
Use acceptance-style tests for complete business scenarios.
Organize acceptance tests by business capability under src/test/java/acceptance.
Prefer in-memory fakes over real infrastructure.
Do not require AWS, DynamoDB, API Gateway, Lambda runtime, or Spring context for domain/use case/acceptance tests.
Keep tests readable using business-oriented given/when/then helpers.
```

---

## 22. Final Rule

The test suite should answer this question:

```text
If I change the code, will I quickly know whether I broke an important business rule?
```

If the answer is yes, the test is valuable.

If the test only checks framework wiring or implementation details, avoid it unless it protects a real risk.
