# Create PR for a backend feature

Read first:
- CLAUDE.md
- docs/PR-CONTEXT.md
- docs/01-ARCHITECTURE.md
- docs/02-ADD-ENDPOINT-CHECKLIST.md
- docs/04-JSON-API-AND-ERRORS.md
- docs/09-PACKAGE-STRUCTURE.md
- docs/10-TESTING-STRATEGY.md
- docs/11-OPENAPI-STRATEGY.md
- docs/data-model/ffresco_backend_decisions_and_dynamodb_setup.md

## Hard rules

- Do not introduce new frameworks.
- Do not modify unrelated files.
- Follow package-by-capability under `com.ffresco.totem.<capability>`.
- Keep business logic in `domain/` and `application/`. Infrastructure stays thin.
- JSON:API + API Gateway events live only in `infrastructure.adapter.in.api`.
- Function `Request`/`Response` records live only in `infrastructure.adapter.in.function` and are plain data carriers.
- Mappers are explicit classes (`<Resource>JsonApiMapper`, `<Action><Resource>FunctionMapper`, `DynamoDb<Entity>Mapper`). No `from`/`toDomain`/`toResponse`/`toEntity` on records or domain objects. No `DTO` suffix.
- Edit `openapi-src/` fragments and re-bundle. Never hand-edit `openapi.yaml`. Register new fragments in `openapi-src/openapi-root.yaml#x-openapi-source`.
- Do not add `HttpApi` `Events` blocks under the function in `template.yaml`.
- All HTTP responses use the JSON:API envelope on **both transports** (Lambda and local Spring MVC). Lambda path: `<Resource>RouteHandler` calls `JsonApiResponseFactory.ok(...)`. Local path: the `@Bean("<beanName>")` exposed to Spring Cloud Function Web MUST return `JsonApiDocument<Attributes>` — never a bare `Function<Request, Response>`. Compose the existing `<Resource>JsonApiMapper` inside the bean factory. `Content-Type: application/vnd.api+json` is set by `JsonApiResponseFactory` (Lambda) and `JsonApiContentTypeAdvice` (local). Domain exceptions that need a stable client `code` implement `CodedDomainException`; error mapping is centralized in `ApiExceptionHandler` (Lambda) and `JsonApiWebExceptionHandler` (local). See [docs/04-JSON-API-AND-ERRORS.md](../docs/04-JSON-API-AND-ERRORS.md).
- DynamoDB access only through output ports + adapters in `infrastructure.adapter.out.dynamodb`. Follow the data-model doc; do not invent new PK/SK/GSI patterns unless the task explicitly asks for a new access pattern — and if so, explain it before implementing.
- Tests: ~70% domain + use case unit, ~20% acceptance, ~10% integration/contract. Each business rule gets happy / invalid / boundary cases. Acceptance tests under `src/test/java/acceptance/<capability>/` with in-memory fakes — no AWS/DynamoDB/Spring context.
- One vertical feature per PR. No architectural rewrites or unrelated moves.
- All API responses must follow JSON:API. Do not return raw domain/application DTOs directly from API endpoints. The `data` shape depends on what the request targets (per JSON:API §7.1):
  - **Single-resource fetch** (`GET /resource/{id}` or any path that targets exactly one resource) → `data` is an **object**.
  - **Collection fetch** (list endpoints) → `data` is an **array** (possibly empty `[]`, never `null`).
  - All current endpoints target a single resource, so they return `data` as an object. Do not wrap a single resource in `[…]`.

  Single-resource success:
```json
        {
        "data": {
            "type": "...",
            "id": "...",
            "attributes": { }
        }
        }
```
  Collection success (only when the endpoint is genuinely a collection):
```json
        {
        "data": [
            {
            "type": "...",
            "id": "...",
            "attributes": { }
            }
        ]
        }
```
  Error responses must use:
```json
        {
        "errors": [
            {
            "status": "...",
            "code": "...",
            "title": "...",
            "detail": "..."
            }
        ]
        }
```
     

## Task

$ARGUMENTS

## Workflow

### Before editing
1. Inspect the existing project style for similar capabilities.
2. Propose the files you will create or modify, grouped by layer (domain → application → infrastructure → openapi → tests).
3. Explain assumptions, especially around DynamoDB key shapes and access patterns.
4. Wait for confirmation unless the user explicitly says to implement directly.

### While editing
- Domain first, then application, then infrastructure, then OpenAPI, then tests. This keeps the dependency direction (`infrastructure → application → domain`) honest at every step.
- Wire new beans in a `<Capability>Config` `@Configuration` class. Do not register beans from across capabilities.
- For local-direct testing, expose `@Bean("<short-name>")` returning `Function<Request, JsonApiDocument<Attributes>>` (or `Supplier<JsonApiDocument<Attributes>>`) in the same config — wrap the existing `<Resource>JsonApiMapper`. Do not expose bare `Function<Request, Response>`. Do not commit changes that flip `spring.cloud.function.definition` in `application.yml`.

### After editing
1. Run `python3 scripts/bundle-openapi.py openapi-src/openapi-root.yaml openapi.yaml` (or `make openapi-bundle`) and confirm the new paths/schemas appear in `openapi.yaml`.
2. Run `mvn test` (or `mvn -Dtest=ClassName test` for a focused run).
3. Run `mvn package -DskipTests` to confirm the shaded AWS jar still builds.
4. Summarize changed files (`git status -u --short`).
5. Report any failed command clearly with the failing output.

### Deliverables in the final response
1. Summary of what was implemented.
2. Files changed (grouped: new vs modified).
3. Tests executed and results.
4. OpenAPI lint / bundle result.
5. Example `curl` for every endpoint created or modified.
6. Postman-ready `curl` examples with headers and a sample body when applicable. Note: when running locally with `mvn -Plocal spring-boot:run`, Spring Cloud Function exposes `Function<I, O>` beans as `POST /<beanName>` accepting the JSON `Request` record as body — even when the deployed endpoint is a `GET`. Provide both shapes when relevant.
