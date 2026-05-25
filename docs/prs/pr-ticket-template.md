# FFresco / Totem SaaS - PR Ticket Template

Use this template to create Jira tickets that can also be pasted into Claude Code using the `/create-pr` command.

The ticket must describe the specific feature/PR only. Do not repeat global architecture rules already defined in:

- `CLAUDE.md`
- `docs/PR-CONTEXT.md`
- `docs/09-PACKAGE-STRUCTURE.md`
- `docs/10-TESTING-STRATEGY.md`
- `docs/11-OPENAPI-STRATEGY.md`
- `docs/12-DYNAMODB-DATA-MODEL.md`

Global rules are assumed. This ticket should define business behavior, API contract, DynamoDB data examples, scenarios, acceptance criteria, and definition of done.

---

# [EPIC X / PR Y] [Short action-oriented title]

Example:

`[Epic 1 / PR 1] Implement Public Menu read API`

---

## Description

### Overview & Context

[Explain briefly why this feature exists and what business/user problem it solves.]

Example:

Customers access the restaurant menu by scanning a table QR code. The backend must resolve the public table id, identify the related branch, and return the active public menu in JSON:API format.

---

### Objective

[Describe the concrete objective of this PR.]

Example:

Implement read-only public menu endpoints for customers using a table QR code.

---

## Scope

### Endpoints

Implement:

```http
[METHOD] [PATH]
[METHOD] [PATH]
```

Example:

```http
GET /public/menu?tableId={tablePublicId}
GET /public/menu/products/{productId}?tableId={tablePublicId}
```

### Capability Package

```text
com.ffresco.totem.[capability]
```

Example:

```text
com.ffresco.totem.publicapi.menu
```

### Expected Use Cases

- `[UseCaseName]`
- `[UseCaseName]`

Example:

- `GetPublicMenuUseCase`
- `GetPublicProductDetailUseCase`

### Expected Ports

Input ports:

- `[InputPortName]`

Output ports:

- `[OutputPortName]`
- `[OutputPortName]`

Example:

Input ports:

- `GetPublicMenuQuery`
- `GetPublicProductDetailQuery`

Output ports:

- `PublicTableReader`
- `PublicMenuReader`

### Expected Adapters

- `[AdapterName]`
- `[AdapterName]`

Example:

- `DynamoDbPublicTableAdapter`
- `DynamoDbPublicMenuAdapter`

### Out of Scope

This PR must not implement:

- [Item not included]
- [Item not included]

Example:

- Order creation
- Payment flow
- Admin menu management
- Stock mutation

---

## Functional Scenarios

Use these scenarios as the source of truth for acceptance-style JUnit tests.

Do not introduce Cucumber.

```gherkin
Scenario: [Successful scenario name]
  Given [initial business state]
  And [additional business state]
  When [user/system action]
  Then [expected successful result]
  And [expected response or side effect]

Scenario: [Error scenario name]
  Given [invalid or missing business state]
  When [user/system action]
  Then [expected error result]
  And the response follows JSON:API error format

Scenario: [Boundary scenario name]
  Given [boundary condition]
  When [user/system action]
  Then [expected boundary behavior]
```

Example:

```gherkin
Scenario: Customer opens the public menu from a valid table QR code
  Given there is an active public table with tablePublicId "tbl_pub_8H7K2X"
  And the table belongs to branch "branch-001"
  And the branch has an active public menu
  And the public menu has available products
  When the customer requests GET /public/menu?tableId=tbl_pub_8H7K2X
  Then the system returns HTTP 200
  And the response follows JSON:API format
  And the response contains the public menu for branch "branch-001"
  And unavailable products are not returned

Scenario: Customer tries to open the public menu with an unknown table id
  Given there is no public table with tablePublicId "tbl_pub_unknown"
  When the customer requests GET /public/menu?tableId=tbl_pub_unknown
  Then the system returns HTTP 404
  And the response follows JSON:API error format
  And the error code is "PUBLIC_TABLE_NOT_FOUND"

Scenario: Customer opens a menu where all products are unavailable
  Given there is an active public table with tablePublicId "tbl_pub_8H7K2X"
  And the branch has an active public menu
  And all products in the public menu are unavailable
  When the customer requests GET /public/menu?tableId=tbl_pub_8H7K2X
  Then the system returns HTTP 200
  And the response follows JSON:API format
  And the categories contain no unavailable products
```

---

## API Contract

### Request Example

```http
[METHOD] [PATH]
Accept: application/vnd.api+json
Content-Type: application/vnd.api+json
```

Example:

```http
GET /public/menu?tableId=tbl_pub_8H7K2X
Accept: application/vnd.api+json
```

### Request Body Example

Use this section only for `POST`, `PUT`, or `PATCH` endpoints.

```json
{
  "data": {
    "type": "example-request",
    "attributes": {}
  }
}
```

### Successful Response Example

```json
{
  "data": {
    "type": "resource-type",
    "id": "resource-id",
    "attributes": {}
  }
}
```

Example:

```json
{
  "data": {
    "type": "public-menu",
    "id": "branch-001-menu",
    "attributes": {
      "branchId": "branch-001",
      "tableId": "tbl_pub_8H7K2X",
      "currency": "BRL",
      "categories": [
        {
          "id": "cat-burgers",
          "name": "Burgers",
          "products": [
            {
              "id": "prd-burger",
              "name": "Cheeseburger",
              "description": "Beef patty.",
              "price": {
                "amount": "12.50",
                "currency": "BRL"
              },
              "available": true
            }
          ]
        }
      ]
    }
  }
}
```

### Error Response Example

```json
{
  "errors": [
    {
      "status": "404",
      "code": "ERROR_CODE",
      "title": "Human readable error title",
      "detail": "Human readable error detail."
    }
  ]
}
```

Example:

```json
{
  "errors": [
    {
      "status": "404",
      "code": "PUBLIC_TABLE_NOT_FOUND",
      "title": "Public table not found",
      "detail": "No public table was found for tableId tbl_pub_unknown."
    }
  ]
}
```

---

## DynamoDB Data Model

Define only the items and access patterns used by this PR.

Do not invent new key patterns if an existing documented pattern already supports the use case.

### Items Involved

#### Item: [Item Name]

```text
PK = ...
SK = ...
entityType = ...
GSI1PK = ...
GSI1SK = ...
GSI2PK = ...
GSI2SK = ...
```

### Example Items

```json
{
  "pk": "TENANT#tenant-001#BRANCH#branch-001",
  "sk": "MENU#PUBLIC",
  "entityType": "PUBLIC_MENU",
  "tenantId": "tenant-001",
  "branchId": "branch-001"
}
```

### Access Patterns

```text
1. [Describe access pattern]
2. [Describe access pattern]
3. [Describe access pattern]
```

Example:

```text
1. Resolve public table by tablePublicId using GSI1.
2. Resolve tenantId, branchId and internal tableId.
3. Read the materialized public menu using tenantId and branchId.
4. Return only available products.
```

---

## Business Rules

- [Rule]
- [Rule]
- [Rule]

Example:

- The backend must resolve `tablePublicId` before reading branch data.
- The public menu must only return products available to customers.
- The public API must not expose DynamoDB `pk`, `sk`, `gsi1pk`, `gsi1sk`, `gsi2pk`, or `gsi2sk`.
- The response must follow JSON:API.

---

## Acceptance Criteria

- [ ] Endpoint `[METHOD] [PATH]` is implemented.
- [ ] Response follows JSON:API format.
- [ ] Error responses follow JSON:API error format.
- [ ] Functional scenarios are covered by acceptance-style JUnit tests.
- [ ] Unit tests cover successful case, invalid case, and boundary case for relevant business rules.
- [ ] OpenAPI source files are updated.
- [ ] Generated `openapi.yaml` is regenerated if needed.
- [ ] `npx @redocly/cli lint openapi.yaml` passes.
- [ ] DynamoDB access follows the documented data model.
- [ ] No DynamoDB physical keys are exposed in the public API contract.
- [ ] No business logic is added to routers, handlers, DTOs, repositories, or infrastructure classes.
- [ ] No unrelated files are modified.
- [ ] Postman-ready curl examples are provided.

---

## Definition of Done

- [ ] Code compiles.
- [ ] Relevant unit tests pass.
- [ ] Relevant acceptance-style tests pass.
- [ ] `mvn test` or the closest available test command passes.
- [ ] `mvn package` passes if possible.
- [ ] OpenAPI lint passes.
- [ ] The PR summary includes changed files.
- [ ] The PR summary includes tests executed and results.
- [ ] The PR summary includes OpenAPI lint result.
- [ ] The PR summary includes one success curl and one error curl per endpoint created or modified.

---

## Claude Code Instructions

Use this section when pasting the ticket into Claude Code with `/create-pr`.

```text
Implement this PR following the project documentation.

Before editing:
1. Read the project instructions and referenced docs.
2. Inspect the current package structure, router style, OpenAPI structure, DynamoDB adapter style, and testing conventions.
3. Propose the files you will create or modify.
4. Explain assumptions.
5. Wait for confirmation before implementing.

After editing:
1. Regenerate OpenAPI if needed.
2. Run `npx @redocly/cli lint openapi.yaml`.
3. Run relevant tests.
4. Run `mvn test` or the closest available test command.
5. Run `mvn package` if possible.
6. Show changed files.
7. Report failed commands clearly.
8. Provide Postman-ready curl examples for every endpoint created or modified.
```
