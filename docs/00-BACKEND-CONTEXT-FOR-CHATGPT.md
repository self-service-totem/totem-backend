# Backend Context for ChatGPT / AI Agents

Use this file as the first context when working on this backend project.

## Project goal

Backend base for a multi-tenant digital menu / ordering system, implemented as a Java 21 AWS Lambda using Spring Cloud Function, API Gateway HTTP API v2, SAM, hexagonal architecture and JSON:API for HTTP input/output.

## Non-negotiable architecture rules

```text
JSON:API lives only in infrastructure.adapter.in.api.
Spring Cloud Function adapters live only in infrastructure.adapter.in.function.
Application and domain must not depend on AWS, API Gateway, Lambda, Jackson, JSON:API or Spring Cloud Function.
Dependency direction: infrastructure -> application -> domain.
```

## Runtime model

### AWS / SAM / API Gateway

```text
API Gateway HTTP API v2
  -> Lambda Java 21
  -> Spring Cloud Function FunctionInvoker
  -> apiGatewayRouter
  -> ApiGatewayRouterFunction
  -> ApiGatewayRouteHandler
  -> JsonApiMapper
  -> Function adapter
  -> UseCase
  -> Domain / ports
```

The AWS/SAM function definition must be:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

### Local direct mode

`application.yml` is kept for local direct Spring Cloud Function testing. Do not change it unless explicitly requested.

Example idea:

```text
POST /listPrice -> simple function request/response
```

Local direct mode does not simulate API Gateway payloads. SAM/API Gateway mode does.

## Package standard

```text
com.ffresco.totem

  domain
    model
    enums
    exception

  application
    port
      in
        <resource>
          <Action><Resource>UseCase
          <Action><Resource>Command
      out
        <NeededPort>
    service
      <resource>
        <Action><Resource>Service

  infrastructure
    adapter
      in
        api
          ApiGatewayRouterFunction
          ApiGatewayRouteHandler
          ApiGatewayRequest
          ApiExceptionHandler
          JsonApiDocument
          JsonApiResource
          JsonApiError
          JsonApiResponseFactory

          <resource>
            <Action><Resource>RouteHandler
            <Resource>JsonApiMapper
            <Resource>Attributes
            <Resource><NestedItem>Attributes

        function
          <resource>
            <Action><Resource>Function
            <Action><Resource>Request
            <Action><Resource>Response

      out
        memory | dynamodb | external

    config
      ApiGatewayConfig
      <Resource>Config
      PersistenceConfig
      JacksonConfig
```

## Naming rules

Do not use `DTO` suffix by default.

```text
Application:        <Action><Resource>UseCase / Command / Service
Function adapter:   <Action><Resource>Function / Request / Response
API adapter:        <Action><Resource>RouteHandler / <Resource>JsonApiMapper / Attributes
JSON:API generic:   JsonApiDocument / JsonApiResource / JsonApiError
```

## JSON:API rules

GET with id in path does not need a JSON:API body.

```text
GET /price-lists/{priceListId}
```

POST/PATCH should receive a JSON:API document:

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

Success response:

```json
{
  "data": {
    "type": "price-lists",
    "id": "default",
    "attributes": {}
  }
}
```

Error response:

```json
{
  "errors": [
    {
      "status": "422",
      "title": "Unprocessable Entity",
      "detail": "Business rule violated"
    }
  ]
}
```

## Error mapping

Centralize exception mapping in `ApiExceptionHandler`.

```text
400 Bad Request        malformed request, missing path param, invalid query, invalid JSON
404 Not Found          resource not found
409 Conflict           duplicate, concurrency or invalid state conflict
422 Unprocessable      syntactically valid request but business rule violation
429 Too Many Requests  rate limit / quota
500 Internal Error     unexpected error
```

## Wiring rule

Prefer explicit wiring with `@Configuration` + `@Bean`, not broad component scanning.

```text
ApiGatewayConfig     -> router, response factory, exception handler
<Resource>Config     -> use case, function, route handler
PersistenceConfig    -> output adapters / ports
JacksonConfig        -> ObjectMapper
```

## OpenAPI / SAM rule

Use `openapi.yaml` as the API contract. `template.yaml` references it with `DefinitionUri`.

```text
openapi.yaml = public API contract
template.yaml = infrastructure and Lambda deployment
Java code = implementation
```

## Checklist for a new endpoint

```text
1. Add path/method to openapi.yaml.
2. Add/confirm HttpApi event in template.yaml if needed.
3. Create <Action><Resource>RouteHandler in infrastructure.adapter.in.api.<resource>.
4. Create/update <Resource>JsonApiMapper and Attributes classes.
5. Create <Action><Resource>Function in infrastructure.adapter.in.function.<resource>.
6. Create <Action><Resource>Request and Response.
7. Create <Action><Resource>UseCase and Command in application.port.in.<resource>.
8. Create <Action><Resource>Service in application.service.<resource>.
9. Add output ports/adapters only if needed.
10. Register beans in <Resource>Config.
11. Add API Gateway event sample for SAM local if useful.
12. Test local direct, then SAM/API Gateway mode.
```

## Mnemonic

```text
Route understands HTTP.
Mapper understands JSON:API.
Function understands Spring Cloud Function.
UseCase understands application action.
Domain understands business rules.
```

