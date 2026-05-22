# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build, test, run

```bash
mvn clean test                       # unit tests
mvn -Dtest=ClassName#method test     # single test
mvn clean package                    # build shaded AWS jar -> target/price-list-lambda-0.0.1-SNAPSHOT-aws.jar
mvn -Plocal spring-boot:run          # run locally as plain HTTP function (uses application.yml)
make openapi-bundle                  # bundle openapi-src/ -> openapi.yaml (required before sam build/package)
make sam-build                       # bundles OpenAPI + sam build
sam deploy                           # deploy (use samconfig.toml; first time: sam deploy --guided)
```

The Maven `package` phase runs `scripts/bundle-openapi.py` automatically (needs `python3` with `PyYAML`).

### Local DynamoDB

```bash
make dynamo-up-local                 # docker compose up dynamodb-local (port 8000)
make dynamo-create-table-local       # create totem-core-local single table
make dynamo-seed-catalog-version-local
```

Required env for local Dynamo: `AWS_REGION=sa-east-1`, `DYNAMODB_ENDPOINT=http://localhost:8000`, `TOTEM_CORE_TABLE_NAME=totem-core-local`.

### Two execution modes

- **Local direct** (`mvn -Plocal spring-boot:run`): Spring Cloud Function Web exposes a single bean named by `spring.cloud.function.definition` in [application.yml](src/main/resources/application.yml). Toggle the bean (`health`, `listPrice`, …) to test a single function with a plain JSON payload. Do not commit changes that flip this — it's intentionally local-only.
- **AWS / SAM**: Lambda runs `SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter`. The `apiGatewayRouter` bean dispatches API Gateway HTTP API v2 events to per-route `RouteHandler`s, which call the Function adapter beans.

## Architecture (non-negotiable)

This is a hexagonal/ports-and-adapters Spring Cloud Function Lambda. Strict layering rules are codified in [.cursor/rules/backend-architecture-mapper-rules.mdc](.cursor/rules/backend-architecture-mapper-rules.mdc) and [docs/01-ARCHITECTURE.md](docs/01-ARCHITECTURE.md). Read those before adding or moving code.

Top-level package is `com.ffresco.totem` (NOT `com.ffresco.pricelist` — README is partially outdated). It contains bounded contexts as siblings:

```
com.ffresco.totem
  common/       cross-cutting: ApiGatewayRouter, JSON:API plumbing, health, exceptions, Money/Currency, configs
  pricelist/    GET /price-lists/{priceListId}
  catalog/      GET /branches/{branchId}/catalog/version
```

Each bounded context follows the same internal shape:

```
<context>/
  domain/        model, enums, exception (no Spring/Jackson/AWS)
  application/   port/in (UseCase, Command), port/out, service (orchestrators)
  infrastructure/
    adapter/in/api/<resource>/        RouteHandler + JsonApiMapper + Attributes
    adapter/in/function/<resource>/   Function + Request + Response + FunctionMapper
    adapter/out/dynamodb|memory/      Persistence adapter + mapper
    config/                           @Configuration wiring
```

Dependency direction is `infrastructure -> application -> domain`. Hard rules:

- JSON:API types and API Gateway event handling exist **only** in `infrastructure.adapter.in.api`.
- Spring Cloud Function `Request`/`Response` records exist **only** in `infrastructure.adapter.in.function`. They are plain data carriers — no `from(domain)` / `toDomain()` methods.
- Mapping between layers lives in **explicit mapper classes** owned by the adapter doing the translation (`<Resource>JsonApiMapper`, `<Resource>FunctionMapper`, `DynamoDb<Entity>Mapper`). Never put `from`/`toDomain`/`toResponse`/`toEntity` on domain objects, request/response records, or persistence items.
- Domain layer must not depend on Spring, Jackson, AWS SDK, JSON:API, or Lambda.
- Application layer must not depend on AWS, Lambda, API Gateway, JSON:API, Jackson, or Spring Cloud Function types.
- Do not use the `DTO` suffix unless asked.
- All HTTP responses use `Content-Type: application/vnd.api+json` with the JSON:API success/error envelope ([docs/04-JSON-API-AND-ERRORS.md](docs/04-JSON-API-AND-ERRORS.md)). Domain/application exceptions are mapped centrally in [ApiExceptionHandler](src/main/java/com/ffresco/totem/common/infrastructure/adapter/in/api/ApiExceptionHandler.java).

### OpenAPI is the contract for API Gateway

[openapi.yaml](openapi.yaml) (bundled from [openapi-src/](openapi-src/)) is the source of truth for HTTP routes. SAM's `PriceListHttpApi` includes it via `AWS::Include`, so API Gateway routes come from OpenAPI — **do not add `HttpApi` `Events` blocks under the function in [template.yaml](template.yaml)**. The OpenAPI integration points all routes at `PriceListFunction`; the `apiGatewayRouter` then dispatches by path/method.

When changing routes, edit files under `openapi-src/` (split spec) and re-bundle; do not hand-edit `openapi.yaml`.

### DynamoDB

Single-table design (`TotemCoreTable`) with `pk`/`sk` and `GSI1` (`gsi1pk`/`gsi1sk`). Table name comes from env `TOTEM_CORE_TABLE_NAME` (set by SAM as `totem-core-${Environment}`; locally `totem-core-local`).

## Adding a new endpoint

Mnemonic: **O-S-A-F-A-C-T** — OpenAPI → SAM → API adapter → Function → Application → Config → Test. Full checklist in [docs/02-ADD-ENDPOINT-CHECKLIST.md](docs/02-ADD-ENDPOINT-CHECKLIST.md). Naming template:

```
<Action><Resource>RouteHandler / <Resource>JsonApiMapper / <Resource>Attributes
<Action><Resource>Function / <Action><Resource>Request / <Action><Resource>Response
<Action><Resource>UseCase / <Action><Resource>Command / <Action><Resource>Service
```

Register beans in the context's `<Resource>Config` (e.g. `PriceListConfig`, `CatalogConfig`) and add the route to the relevant `Resource`s in `apiGatewayRouter` wiring.

## Rules I follow on every PR

Source-of-truth docs (do not duplicate them here):

- [docs/PR-CONTEXT.md](docs/PR-CONTEXT.md) — PR scope and future-domain split.
- [docs/09-PACKAGE-STRUCTURE.md](docs/09-PACKAGE-STRUCTURE.md) — capability-first packaging, `common/` discipline.
- [docs/10-TESTING-STRATEGY.md](docs/10-TESTING-STRATEGY.md) — pyramid, domain/use-case focus, acceptance layout.
- [docs/11-OPENAPI-STRATEGY.md](docs/11-OPENAPI-STRATEGY.md) — OpenAPI authoring strategy.

Condensed rules:

- **Layering**: `infrastructure → application → domain`. Domain has no Spring/Jackson/AWS/JSON:API/Lambda. Application has no AWS/Lambda/API Gateway/JSON:API/Jackson/Spring Cloud Function.
- **Adapter ownership**: JSON:API + API Gateway events live only in `infrastructure.adapter.in.api`; Spring Cloud Function `Request`/`Response` live only in `infrastructure.adapter.in.function`; persistence lives only in `infrastructure.adapter.out.*`.
- **Mappers are explicit classes** (`<Resource>JsonApiMapper`, `<Action><Resource>FunctionMapper`, `DynamoDb<Entity>Mapper`). No `from`/`toDomain`/`toResponse`/`toEntity` on domain objects, request/response records, or persistence items. Records stay plain data carriers. No `DTO` suffix.
- **Capability-first packages** under `com.ffresco.totem.<capability>`. `common/` only for stable cross-cutting code (`Money`, `Currency`, JSON:API plumbing, router, exceptions).
- **HTTP**: `Content-Type: application/vnd.api+json` + JSON:API envelope on **both transports**. Lambda: `<Resource>RouteHandler` + `JsonApiResponseFactory`. Local (`mvn -Plocal`): the `@Bean("<beanName>")` exposed to Spring Cloud Function Web returns `JsonApiDocument<Attributes>` (never a bare `Function<Request, Response>`). `data` shape follows JSON:API §7.1 — **object** for single-resource fetches (all current endpoints), **array** for collection fetches. Never wrap a single resource in `[…]`. Errors mapped centrally in `ApiExceptionHandler` (Lambda) and `JsonApiWebExceptionHandler` (local). Domain exceptions that need a stable client-facing error code implement `CodedDomainException` (see [docs/04-JSON-API-AND-ERRORS.md](docs/04-JSON-API-AND-ERRORS.md)).
- **OpenAPI/SAM**: edit `openapi-src/` and re-bundle; never hand-edit `openapi.yaml`. Don't add `HttpApi` `Events` to the function in `template.yaml`. Lambda env stays `SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter`. `application.yml` is local-only — don't commit toggles.
- **DynamoDB**: single-table `TotemCoreTable` (`pk`/`sk` + `GSI1`); accessed only through output ports + adapters in `infrastructure.adapter.out.dynamodb`.
- **Tests**: ~70% domain + use-case unit / ~20% acceptance / ~10% integration-contract. Each business rule gets happy / invalid / boundary cases. Acceptance tests under `src/test/java/acceptance/<capability>/` with in-memory fakes — no AWS/DynamoDB/Spring context. Keep adapter/config/mapper tests thin.
- **PR scope**: one vertical feature per PR; no architectural rewrites, unrelated moves, extra endpoints, business logic in routers/functions, or DynamoDB calls outside out-adapters.
