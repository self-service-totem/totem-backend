# CLAUDE.md

This repository uses a documentation-first workflow.

## Read first

```text
docs/00-index.md
```

## Canonical standards

```text
docs/01-architecture-standard.md
docs/02-testing-strategy.md
docs/03-data-model.md
docs/04-api-contract-standard.md
docs/05-local-aws-runbook.md
```

## Backend PR masterplan

```text
docs/planning/masterplan_prs_api_v1.md
```

Use it to understand the ordered roadmap and the human/business goal of each PR. Do not treat it as the canonical source for architecture, testing, API contract or data modeling rules.

## Ticket workflow

Tickets follow:

```text
docs/docs/tickets/pr-ticket-template.md
```

Implementation command:

```text
/create-pr
```

## Core rule

```text
Infrastructure adapts. Application orchestrates. Domain decides.
```

Dependency direction:

```text
infrastructure -> application -> domain
```

## Runtime rule

AWS/SAM Lambda must use:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

## Implementation rule

Do not duplicate architecture, testing, data model or API contract rules inside tickets or prompts. Reference the canonical docs instead.


## API versioning

Use path-based API versioning for every HTTP endpoint.

Current version:

```text
/v1
```

The canonical rule lives in:

```text
docs/04-api-contract-standard.md
```

Never create unversioned routes.
