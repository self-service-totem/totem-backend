# /create-pr

Implement one vertical backend PR from a Jira-style ticket.

## Required input

The user must paste a ticket following:

```text
docs/tickets/pr-ticket-template.md
```

## Read first

Read these files before editing:

```text
CLAUDE.md
docs/00-index.md
docs/01-architecture-standard.md
docs/02-testing-strategy.md
docs/03-data-model.md
docs/04-api-contract-standard.md
```

Read this only if you need build/deploy/local execution details:

```text
docs/05-local-aws-runbook.md
```

## Task

Implement exactly the feature described in the ticket.

## Hard rules

- One vertical feature per PR.
- Do not implement out-of-scope items.
- Do not introduce frameworks.
- Do not modify unrelated files.
- Follow package-by-capability under `com.ffresco.totem.<capability>`.
- Follow hexagonal dependency direction: `infrastructure -> application -> domain`.
- Keep business logic out of route handlers, mappers, adapters, repositories and config classes.
- Keep JSON:API and API Gateway event handling only in `infrastructure.adapter.in.api`.
- Keep Spring Cloud Function request/response adapters only in `infrastructure.adapter.in.function`.
- Use explicit mapper classes. Do not add `from`, `toDomain`, `toResponse`, `toEntity` methods to records/domain objects.
- Do not use `DTO` suffix unless explicitly requested.
- Edit OpenAPI source fragments, not generated `openapi.yaml` directly.
- Do not add API Gateway routes directly in SAM `Events` unless explicitly requested.
- DynamoDB access only through output ports and adapters.
- Do not invent PK/SK/GSI patterns without explaining the access pattern.

## Workflow

### Before editing

1. Summarize the requested PR in 5 lines.
2. Identify capability package.
3. Identify endpoints.
4. Identify use cases, ports and adapters.
5. Identify data model impact.
6. Identify tests required from Gherkin scenarios.

### While editing

Implement in this order:

```text
1. OpenAPI fragments
2. Domain model/service if needed
3. Application input port/use case
4. Output ports
5. Adapter out if needed
6. Function adapter + mapper
7. API route handler + JSON:API mapper
8. Config beans
9. Tests
```

### After editing

Run:

```bash
mvn test
```

If OpenAPI changed, run the project-specific OpenAPI bundle command.

## Final response

Return:

```text
- Summary of what changed
- Files changed
- Tests added/updated
- Commands run and result
- Any assumptions or pending items
```
