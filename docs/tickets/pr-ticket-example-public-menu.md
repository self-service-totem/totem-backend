# [Epic 1 / PR 1] Implement Public Menu Read API

## Description

### Overview & Context

Customers access the restaurant menu by scanning a table QR code. The backend must resolve the public table id, identify the related branch, and return the active public menu in JSON:API format.

### Objective

Implement read-only public menu endpoints for customers using a table QR code.

## Scope

### Endpoints

```http
GET /v1/public/menu?tableId={tablePublicId}
GET /v1/public/menu/products/{productId}?tableId={tablePublicId}
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

## Functional Scenarios

```gherkin
Scenario: Customer opens the public menu from a valid table QR code
  Given there is an active public table with tablePublicId "tbl_pub_8H7K2X"
  And the table belongs to branch "branch-001"
  And the branch has an active public menu
  And the public menu has available products
  When the customer requests GET /v1/public/menu?tableId=tbl_pub_8H7K2X
  Then the system returns HTTP 200
  And the response follows JSON:API format
  And the response contains the public menu for branch "branch-001"
  And unavailable products are not returned

Scenario: Customer tries to open the public menu with an unknown table id
  Given there is no public table with tablePublicId "tbl_pub_unknown"
  When the customer requests GET /v1/public/menu?tableId=tbl_pub_unknown
  Then the system returns HTTP 404
  And the response follows JSON:API error format
  And the error code is "PUBLIC_TABLE_NOT_FOUND"

Scenario: Customer opens a menu where all products are unavailable
  Given there is an active public table with tablePublicId "tbl_pub_8H7K2X"
  And the branch has an active public menu
  And all products in the public menu are unavailable
  When the customer requests GET /v1/public/menu?tableId=tbl_pub_8H7K2X
  Then the system returns HTTP 200
  And the response follows JSON:API format
  And the categories contain no unavailable products
```

## API Contract

### Request Example

```http
GET /v1/public/menu?tableId=tbl_pub_8H7K2X
Accept: application/vnd.api+json
```

### Successful Response Example

```json
{
  "data": {
    "type": "public-menu",
    "id": "branch-001-public-menu",
    "attributes": {
      "branchId": "branch-001",
      "categories": []
    }
  }
}
```

### Error Responses

| Case | HTTP status | Error code |
|---|---:|---|
| Missing tableId | 400 | MISSING_TABLE_ID |
| Unknown public table | 404 | PUBLIC_TABLE_NOT_FOUND |
| No active public menu | 404 | PUBLIC_MENU_NOT_FOUND |

## Data Model Impact

```text
Yes
```

### Entities affected

- `RestaurantTable`
- `PublicMenu`

### Access patterns

```text
Find public table by public table id.
Read active public menu by tenantId + branchId.
```

### DynamoDB keys / indexes

Public table lookup:

```text
GSI1PK = PUBLIC_TABLE#<tablePublicId>
GSI1SK = TENANT#<tenantId>#BRANCH#<branchId>#TABLE#<tableId>
```

Public menu read model:

```text
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = MENU#PUBLIC
```

## Business Rules

- A public menu can only be returned for a known active public table.
- Unavailable products must not be returned.
- The public endpoint must not expose internal tenant administration data.
- The backend resolves tenant and branch from the public table id.

## Test Plan

### Unit tests

- Valid public table returns active menu.
- Unknown public table is rejected.
- Missing active menu is rejected.
- Unavailable products are filtered.

### Acceptance-style tests

- Customer opens menu with valid table QR.
- Customer opens menu with unknown table id.
- Customer opens menu when all products are unavailable.

### Contract / integration / smoke tests

- JSON:API response object for single public menu.
- JSON:API error response for not found.
- OpenAPI path and query parameter contract.

## Definition of Done

- [ ] One vertical public menu read feature implemented.
- [ ] OpenAPI fragments updated.
- [ ] JSON:API success and error responses respected.
- [ ] DynamoDB access patterns follow `docs/03-data-model.md`.
- [ ] Gherkin scenarios covered by tests.
- [ ] `mvn test` passes.
