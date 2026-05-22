# OpenAPI Strategy

This document defines how OpenAPI must be maintained in this project.

The goal is to keep the API contract clear, versionable, and aligned with the backend implementation without creating unnecessary fragmentation.

---

## Core Principle

OpenAPI is the source of truth for the HTTP contract.

Every public endpoint, request body, response body, error response, path parameter, query parameter, and tag must be represented in OpenAPI.

The backend implementation must follow the OpenAPI contract.

Do not implement endpoints that are not documented in OpenAPI.
Do not change request/response shapes without updating OpenAPI.

---

## File Strategy

The project uses a generated OpenAPI file consumed by SAM/API Gateway.

Editable source files live under:

```text
openapi-src/
```

The generated file is:

```text
openapi.yaml
```

Rules:

```text
Edit openapi-src/ files.
Do not manually edit generated openapi.yaml unless explicitly required.
Regenerate openapi.yaml using the project OpenAPI bundle script.
SAM/API Gateway consumes openapi.yaml.
```

---

## Recommended Structure

Use a lightweight split by business capability.

Recommended structure:

```text
openapi-src/
  openapi-root.yaml
  paths/
    health.yaml
    public-menu.yaml
    public-order.yaml
    catalog.yaml
    pricelist.yaml
    stock.yaml
    operations.yaml
  components/
    schemas/
      common.yaml
      errors.yaml
      public-menu.yaml
      public-order.yaml
      catalog.yaml
      pricelist.yaml
      stock.yaml
    responses/
      common.yaml
    parameters/
      common.yaml
```

Avoid excessive fragmentation.

Do not create one file per tiny schema unless the file is clearly becoming too large.

Preferred split:

```text
By capability first.
By technical type second.
```

Example:

```text
paths/public-menu.yaml
components/schemas/public-menu.yaml
```

Avoid:

```text
components/schemas/public-menu/product-name.yaml
components/schemas/public-menu/product-price.yaml
components/schemas/public-menu/product-description.yaml
```

---

## When To Split More

Only split OpenAPI further when there is a real maintainability problem.

Examples:

```text
A file becomes too large to read comfortably.
Multiple PRs often conflict in the same file.
A capability has many endpoints and schemas.
Cursor/Claude loses context because the OpenAPI file is too large.
```

Until then, keep the structure simple.

---

## Capability Tags

Every endpoint must have a clear tag matching the business capability.

Examples:

```yaml
tags:
  - name: Health
  - name: Public Menu
  - name: Public Orders
  - name: Catalog
  - name: Price List
  - name: Stock
  - name: Operations
```

Example endpoint:

```yaml
/public/menu:
  get:
    tags:
      - Public Menu
```

---

## JSON:API Response Strategy

All API responses should follow the project JSON:API style.

Successful single-resource response:

```json
{
  "data": {
    "type": "public-product",
    "id": "product-001",
    "attributes": {
      "name": "Coca-Cola",
      "description": "350ml can"
    }
  }
}
```

Successful collection or aggregate response:

```json
{
  "data": {
    "type": "public-menu",
    "id": "branch-001-menu",
    "attributes": {
      "branchId": "branch-001",
      "currency": "BRL",
      "categories": []
    }
  }
}
```

Error response:

```json
{
  "errors": [
    {
      "status": "404",
      "code": "PUBLIC_TABLE_NOT_FOUND",
      "title": "Public table not found",
      "detail": "No public table was found for tableId table-public-999."
    }
  ]
}
```

Rules:

```text
Use data for successful responses.
Use errors for error responses.
Do not mix data and errors in the same response.
Use stable type values.
Use explicit error codes.
```

---

## Error Response Strategy

Every endpoint must document common error responses.

At minimum, document when applicable:

```text
400 Bad Request
404 Not Found
409 Conflict
500 Internal Server Error
```

Use JSON:API error objects.

Common error schema should live in:

```text
openapi-src/components/schemas/errors.yaml
```

Common responses should live in:

```text
openapi-src/components/responses/common.yaml
```

Example error codes:

```text
PUBLIC_TABLE_NOT_FOUND
PUBLIC_PRODUCT_NOT_FOUND
INVALID_REQUEST
BUSINESS_RULE_VIOLATION
INTERNAL_ERROR
```

---

## Parameters

Reusable parameters should be defined once.

Examples:

```text
tableId
productId
branchId
tenantId
```

Public endpoints should not expose internal IDs unless the use case explicitly requires it.

For public QR flows, prefer public IDs such as:

```text
tablePublicId
```

Example:

```http
GET /public/menu?tableId={tablePublicId}
```

The backend resolves the public table ID to:

```text
tenantId
branchId
tableId
active menu
```

---

## DynamoDB Alignment

OpenAPI must reflect the public contract, not the internal DynamoDB model.

Do not leak DynamoDB key names into the API contract.

Do not expose:

```text
pk
sk
gsi1pk
gsi1sk
gsi2pk
gsi2sk
entityType
```

Those fields belong to the persistence layer only.

The PR prompt may include DynamoDB examples for implementation clarity, but the API response should use business-friendly fields.

---

## OpenAPI and Package Alignment

Endpoint groups should align with backend capabilities.

Example:

```text
OpenAPI tag: Public Menu
OpenAPI path file: paths/public-menu.yaml
Schema file: components/schemas/public-menu.yaml
Backend package: com.ffresco.totem.publicapi.menu
Acceptance tests: src/test/java/acceptance/publicapi/menu
```

This keeps the contract, implementation, and tests aligned.

---

## Generated File Rule

The generated file is consumed by SAM/API Gateway:

```text
openapi.yaml
```

Rules:

```text
Do not edit openapi.yaml manually.
Update openapi-src/ instead.
Run the OpenAPI bundle script.
Commit both source changes and generated openapi.yaml if the repository convention requires generated files to be committed.
```

If the project build is configured correctly, OpenAPI generation should run during:

```text
mvn generate-resources
mvn package
sam build
```

If generation fails, report the error clearly instead of manually patching generated output.

---

## Claude Code / Cursor Rules

When implementing a PR:

```text
Read this document before editing OpenAPI.
Update the OpenAPI source files, not only the generated file.
Do not invent a new OpenAPI structure.
Do not split files excessively.
Do not change existing endpoint contracts unless the PR explicitly asks for it.
Do not introduce a new response style.
Use JSON:API responses consistently.
Document success and error responses.
Regenerate openapi.yaml if the project has a bundle script.
Report any OpenAPI generation failure.
```

---

## PR Checklist

For every PR that adds or changes an endpoint, verify:

```text
[ ] Endpoint path is documented.
[ ] HTTP method is documented.
[ ] Tags are correct.
[ ] Query/path parameters are documented.
[ ] Request body is documented when applicable.
[ ] Successful JSON:API response is documented.
[ ] Error JSON:API responses are documented.
[ ] Schemas are placed in the correct capability file.
[ ] OpenAPI source files were updated.
[ ] Generated openapi.yaml was regenerated if required.
[ ] Backend route mapping matches OpenAPI.
[ ] Acceptance-style tests match the documented behavior.
```

---

## Final Rule

OpenAPI should help the project move faster, not slow it down.

Keep it clear, consistent, and business-oriented.

Use a lightweight split by capability.
Avoid unnecessary fragmentation.
