# [Epic X / PR Y] [Short action-oriented title]

## Description

### Overview & Context

[Explain briefly why this feature exists and what business/user problem it solves.]

### Objective

[Describe the concrete objective of this PR.]

## Scope

### Endpoints

```http
[METHOD] [PATH]
```

### Capability Package

```text
com.ffresco.totem.<capability>
```

### Expected Use Cases

- `<Action><Resource>UseCase`

### Expected Ports

Input ports:

- `<Action><Resource>UseCase | Query | Command`

Output ports:

- `<NeededPort>`

### Expected Adapters

- `<AdapterName>`

### Out of Scope

This PR must not implement:

- [Item not included]
- [Item not included]

## Functional Scenarios

Use these scenarios as the source of truth for acceptance-style JUnit tests.

Do not introduce Cucumber. Gherkin is used as a shared language for Jira, humans and AI.

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

## API Contract

### Request Example

```http
[METHOD] [PATH]
Accept: application/vnd.api+json
Content-Type: application/vnd.api+json
```

### Request Body Example

Use only for POST, PUT or PATCH.

```json
{
  "data": {
    "type": "resource-type",
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

### Error Responses

| Case | HTTP status | Error code |
|---|---:|---|
| [case] | 400 | EXAMPLE_ERROR_CODE |
| [case] | 404 | EXAMPLE_NOT_FOUND |

## Data Model Impact

Does this PR change or use persistence?

```text
Yes / No
```

If yes, specify only the concrete access patterns for this PR.

### Entities affected

- [Entity]

### Access patterns

```text
[Example]
Find public table by public table id.
Read active public menu by tenantId + branchId.
```

### DynamoDB keys / indexes

```text
PK = ...
SK = ...
GSI1PK = ...
GSI1SK = ...
```

### Example items

```json
{
  "pk": "...",
  "sk": "...",
  "entityType": "..."
}
```

## Business Rules

- [Rule 1]
- [Rule 2]
- [Rule 3]

## Test Plan

### Unit tests

- [Domain rule: happy / invalid / boundary]
- [Use case behavior]

### Acceptance-style tests

- [Scenario mapped from Gherkin]
- [Scenario mapped from Gherkin]

### Contract / integration / smoke tests

- [OpenAPI/JSON:API contract]
- [DynamoDB adapter mapping if relevant]
- [Route wiring if relevant]

## Definition of Done

- [ ] One vertical feature implemented.
- [ ] No unrelated refactor or architecture rewrite.
- [ ] OpenAPI fragments updated if endpoint changes.
- [ ] JSON:API success and error responses respected.
- [ ] Architecture standard respected.
- [ ] Data model standard respected if persistence is touched.
- [ ] Gherkin scenarios covered by tests.
- [ ] `mvn test` passes.
- [ ] No AWS/Spring/DynamoDB dependency in domain tests.
- [ ] No business logic in route handlers, mappers, adapters or configs.
