# JSON:API and Errors

## Content type

```text
Content-Type: application/vnd.api+json
```

## Success response

### `data` shape: object vs array

Per [JSON:API §7.1 — Top Level](https://jsonapi.org/format/#document-top-level), the
shape of `data` depends on the **target** of the request, not on a project-wide
convention:

| Endpoint kind | `data` shape | Example |
|---|---|---|
| Single resource fetch (`GET /resource/{id}`, or any path that targets exactly one resource — e.g. `GET /public/menu?tableId=…` returns *the* menu of one table) | **object** `{}` | `data: { "type": "...", "id": "...", "attributes": { … } }` |
| Resource collection (`GET /resources`, list/search endpoints) | **array** `[]` (possibly empty) | `data: [ { "type": "...", "id": "...", "attributes": { … } }, … ]` |
| To-one relationship | object or `null` | `data: { "type": "...", "id": "..." }` |
| To-many relationship | array (possibly empty) | `data: [ { "type": "...", "id": "..." } ]` |

All current endpoints in this service target a single resource, so they return
`data` as an **object**. Returning `[{…}]` for a single-resource fetch violates
the spec and forces clients into a defensive `response.data[0]` pattern.

When adding a new collection endpoint:

- Use `JsonApiCollectionDocument<T>` (to be added when the first collection
  endpoint lands) — do not reuse `JsonApiDocument<T>` and force an array onto it.
- Empty collections return `data: []`, never `data: null`.

### Single-resource example

```json
{
  "data": {
    "type": "price-lists",
    "id": "default",
    "attributes": {
      "products": []
    }
  }
}
```

## Error response

```json
{
  "errors": [
    {
      "status": "400",
      "title": "Bad Request",
      "detail": "Path parameter 'priceListId' is required"
    }
  ]
}
```

### Optional `code` field

Errors may include a stable application-level `code` so clients can branch on a
specific failure without parsing `detail`. The field is optional (omitted when
null via `@JsonInclude(NON_NULL)`).

```json
{
  "errors": [
    {
      "status": "404",
      "code": "PUBLIC_TABLE_NOT_FOUND",
      "title": "Public table not found",
      "detail": "No public table was found for tableId tbl-public-001."
    }
  ]
}
```

A domain exception that should expose a `code` (and a custom `title`) to the
HTTP boundary implements `CodedDomainException`:

```java
public class PublicTableNotFoundException
        extends ResourceNotFoundException
        implements CodedDomainException {

    public static final String CODE  = "PUBLIC_TABLE_NOT_FOUND";
    public static final String TITLE = "Public table not found";

    @Override public String code()  { return CODE; }
    @Override public String title() { return TITLE; }
}
```

Conventions:

- Codes are `SCREAMING_SNAKE_CASE`, scoped by capability (`PUBLIC_TABLE_NOT_FOUND`,
  `CATALOG_VERSION_CONFLICT`, …).
- The HTTP status mapping still comes from the exception type (`ResourceNotFoundException` → 404,
  etc.), not from the code.
- Centralized in `ApiExceptionHandler` — never set `code`/`title` from a route handler.
- `JsonApiResponseFactory` exposes overloads such as `notFound(code, detail)` and
  `notFound(code, title, detail)`; prefer those when responding from the handler.

## Two transports, one mapping

Both successful and error responses must produce a JSON:API envelope (`Content-Type: application/vnd.api+json`) on **both** execution modes:

| Mode | Entry point | Success wrapper | Error handler | Content-Type |
|------|-------------|-----------------|---------------|--------------|
| AWS Lambda / API Gateway | `ApiGatewayRouterFunction` (`SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter`) | `<Resource>RouteHandler` + `JsonApiResponseFactory` | `ApiExceptionHandler` | set by `JsonApiResponseFactory` |
| Local (`mvn -Plocal spring-boot:run`) | Spring MVC's `FunctionController` (Spring Cloud Function Web) — exposes each `Function<I,O>` as `POST /<beanName>` | local-direct `@Bean` returns `JsonApiDocument<Attributes>` | `JsonApiWebExceptionHandler` (`@RestControllerAdvice`) | set by `JsonApiContentTypeAdvice` (`ResponseBodyAdvice`) |

The local mode bypasses the API Gateway router entirely. Without the
`@RestControllerAdvice` and the local-direct beans returning `JsonApiDocument`,
responses surface either as Spring's default 500 (errors) or as the bare
`Response` record fields (success) — both bypass the JSON:API envelope.

**Rule when adding a new endpoint:**

1. Define the `<Resource>JsonApiMapper.toResource(Response) -> JsonApiResource<Attributes>` in `infrastructure.adapter.in.api`.
2. The `<Resource>RouteHandler` calls `JsonApiResponseFactory.ok(...)` for the Lambda path.
3. The local-direct bean in `<Capability>Config` MUST return `JsonApiDocument<Attributes>`, composing the `Function<I,O>` with the same `<Resource>JsonApiMapper`:

   ```java
   @Bean("publicMenu")
   public Function<GetPublicMenuRequest, JsonApiDocument<PublicMenuAttributes>> publicMenu(
           GetPublicMenuFunction function
   ) {
       return request -> new JsonApiDocument<>(
               PublicMenuJsonApiMapper.toResource(function.apply(request))
       );
   }
   ```

   Never expose a bare `Function<Request, Response>` as the local-direct bean —
   that leaks the plain function record to clients and bypasses the envelope.

**Rule when adding a new domain exception family:**

1. Add the mapping branch in `ApiExceptionHandler#handle`.
2. Add a matching `@ExceptionHandler` method in `JsonApiWebExceptionHandler`.
3. Both must produce the same HTTP status, the same JSON:API envelope, the same
   `code`/`title` (read from `CodedDomainException` if implemented).
4. Add a unit test in both `ApiExceptionHandlerTest`-style and
   `JsonApiWebExceptionHandlerTest`-style locations covering happy + coded cases.

Never catch domain exceptions inside route handlers, services, or function
adapters to "translate" them locally — that defeats both handlers.

## GET vs POST/PATCH

GET by path id:

```text
GET /price-lists/{priceListId}
```

No JSON:API request body needed.

POST/PATCH body:

```json
{
  "data": {
    "type": "price-lists",
    "attributes": {
      "name": "Lista verano",
      "currency": "BRL"
    }
  }
}
```

## Error mapping

```text
400 Bad Request        malformed request, missing path param, invalid query, invalid JSON
404 Not Found          resource not found
409 Conflict           duplicate, concurrency or invalid state conflict
422 Unprocessable      syntactically valid request but business rule violation
429 Too Many Requests  rate limit / quota
500 Internal Error     unexpected error
```

## Future JSON:API evolution

Current simplification: nested products can live in `attributes.products`.

Future stricter JSON:API modeling can move nested resources to:

```text
relationships
included
links
meta
pagination
filters
```
