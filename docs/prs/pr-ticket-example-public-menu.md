# [Epic 1 / PR 1] Implement Public Menu Read API

---

## Description

### Overview & Context

Customers access the restaurant menu by scanning a table QR code. The backend must resolve the public table id, identify the related tenant and branch, and return the active public menu in JSON:API format.

This is the first customer-facing read-only endpoint for the public menu experience.

---

### Objective

Implement read-only public menu endpoints for customers using a table QR code.

---

## Scope

### Endpoints

Implement:

```http
GET /public/menu?tableId={tablePublicId}
GET /public/menu/products/{productId}?tableId={tablePublicId}
```

### Capability Package

```text
com.ffresco.totem.publicapi.menu
```

### Expected Use Cases

- `GetPublicMenuUseCase`
- `GetPublicProductDetailUseCase`

### Expected Ports

Input ports:

- `GetPublicMenuQuery`
- `GetPublicProductDetailQuery`

Output ports:

- `PublicTableReader`
- `PublicMenuReader`

### Expected Adapters

- `DynamoDbPublicTableAdapter`
- `DynamoDbPublicMenuAdapter`

### Out of Scope

This PR must not implement:

- Order creation
- Payment flow
- Admin menu management
- Stock mutation
- Kitchen operations

---

## Functional Scenarios

Use these scenarios as the source of truth for acceptance-style JUnit tests.

Do not introduce Cucumber.

```gherkin
Scenario: Customer opens the public menu from a valid table QR code
  Given there is an active public table with tablePublicId "tbl_pub_8H7K2X"
  And the table belongs to tenant "tenant-001" and branch "branch-001"
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

Scenario: Customer opens a valid public product detail
  Given there is an active public table with tablePublicId "tbl_pub_8H7K2X"
  And the table belongs to tenant "tenant-001" and branch "branch-001"
  And product "prd-burger" belongs to the active public menu
  When the customer requests GET /public/menu/products/prd-burger?tableId=tbl_pub_8H7K2X
  Then the system returns HTTP 200
  And the response follows JSON:API format
  And the response contains product "prd-burger"

Scenario: Customer opens a product that is not part of the public menu
  Given there is an active public table with tablePublicId "tbl_pub_8H7K2X"
  And product "prd-unknown" does not belong to the active public menu
  When the customer requests GET /public/menu/products/prd-unknown?tableId=tbl_pub_8H7K2X
  Then the system returns HTTP 404
  And the response follows JSON:API error format
  And the error code is "PUBLIC_PRODUCT_NOT_FOUND"
```

---

## API Contract

### Request Example: Public Menu

```http
GET /public/menu?tableId=tbl_pub_8H7K2X
Accept: application/vnd.api+json
```

### Successful Response Example: Public Menu

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

### Request Example: Product Detail

```http
GET /public/menu/products/prd-burger?tableId=tbl_pub_8H7K2X
Accept: application/vnd.api+json
```

### Successful Response Example: Product Detail

```json
{
  "data": {
    "type": "public-product",
    "id": "prd-burger",
    "attributes": {
      "branchId": "branch-001",
      "tableId": "tbl_pub_8H7K2X",
      "name": "Cheeseburger",
      "description": "Beef patty.",
      "price": {
        "amount": "12.50",
        "currency": "BRL"
      },
      "available": true,
      "categoryId": "cat-burgers",
      "categoryName": "Burgers"
    }
  }
}
```

### Error Response Example: Unknown Table

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

### Error Response Example: Product Not Found

```json
{
  "errors": [
    {
      "status": "404",
      "code": "PUBLIC_PRODUCT_NOT_FOUND",
      "title": "Public product not found",
      "detail": "Product prd-unknown is not available in the public menu for this table."
    }
  ]
}
```

---

## DynamoDB Data Model

Define only the items and access patterns used by this PR.

Do not invent new key patterns if an existing documented pattern already supports the use case.

### Items Involved

#### Item: Public Table Lookup

```text
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = TABLE#<tableId>
entityType = PUBLIC_TABLE
GSI1PK = PUBLIC_TABLE#<tablePublicId>
GSI1SK = TENANT#<tenantId>#BRANCH#<branchId>#TABLE#<tableId>
```

#### Item: Materialized Public Menu

```text
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = MENU#PUBLIC
entityType = PUBLIC_MENU
```

### Example Items

```json
{
  "pk": "TENANT#tenant-001#BRANCH#branch-001",
  "sk": "TABLE#tbl-001",
  "entityType": "PUBLIC_TABLE",
  "tenantId": "tenant-001",
  "branchId": "branch-001",
  "tableId": "tbl-001",
  "tablePublicId": "tbl_pub_8H7K2X",
  "tableName": "Mesa 1",
  "active": true,
  "gsi1pk": "PUBLIC_TABLE#tbl_pub_8H7K2X",
  "gsi1sk": "TENANT#tenant-001#BRANCH#branch-001#TABLE#tbl-001"
}
```

```json
{
  "pk": "TENANT#tenant-001#BRANCH#branch-001",
  "sk": "MENU#PUBLIC",
  "entityType": "PUBLIC_MENU",
  "tenantId": "tenant-001",
  "branchId": "branch-001",
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
        },
        {
          "id": "prd-disabled",
          "name": "Disabled product",
          "description": "Not available now.",
          "price": {
            "amount": "10.00",
            "currency": "BRL"
          },
          "available": false
        }
      ]
    }
  ]
}
```

### Access Patterns

```text
1. Resolve public table by tablePublicId using GSI1.
2. Resolve tenantId, branchId and internal tableId.
3. Read the materialized public menu using tenantId and branchId.
4. Return only available products in the public response.
5. For product detail, verify that productId belongs to the resolved branch public menu.
```

---

## Business Rules

- The backend must resolve `tablePublicId` before reading branch menu data.
- The public menu endpoint must return only products available to customers.
- Product detail must only return products that belong to the active public menu for the resolved table/branch.
- The public API must not expose DynamoDB `pk`, `sk`, `gsi1pk`, `gsi1sk`, `gsi2pk`, or `gsi2sk`.
- All successful and error responses must follow JSON:API.

---

## Acceptance Criteria

- [ ] `GET /public/menu?tableId={tablePublicId}` is implemented.
- [ ] `GET /public/menu/products/{productId}?tableId={tablePublicId}` is implemented.
- [ ] Successful responses follow JSON:API format.
- [ ] Error responses follow JSON:API error format.
- [ ] Unknown public table returns `PUBLIC_TABLE_NOT_FOUND`.
- [ ] Unknown or unavailable product returns `PUBLIC_PRODUCT_NOT_FOUND`.
- [ ] Unavailable products are not returned in the public menu response.
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
