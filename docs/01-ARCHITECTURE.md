# Architecture

## Goal

Uniform backend architecture for Java 21 + Spring Cloud Function + AWS Lambda + API Gateway HTTP API v2 + SAM + JSON:API + hexagonal architecture.

## Core rule

```text
Infrastructure adapts. Application orchestrates. Domain decides.
```

Dependency direction:

```text
infrastructure -> application -> domain
```

## AWS flow

```text
API Gateway HTTP API v2
  -> Lambda
  -> FunctionInvoker
  -> apiGatewayRouter
  -> ApiGatewayRouterFunction
  -> ApiGatewayRouteHandler
  -> JsonApiMapper
  -> Spring Cloud Function adapter
  -> UseCase
  -> Domain
  -> Port out
  -> Adapter out
```

## Local direct flow

`application.yml` is for local fast testing with Spring Cloud Function Web. It can expose one direct function, such as `listPrice` or `health`, without sending an API Gateway event payload.

```text
Local direct:
  POST /listPrice
  -> GetPriceListFunction
  -> GetPriceListUseCase
```

AWS/SAM mode uses the router:

```text
AWS/API Gateway:
  GET /price-lists/{priceListId}
  -> apiGatewayRouter
  -> GetPriceListRouteHandler
  -> GetPriceListFunction
  -> GetPriceListUseCase
```

## Package map

```text
com.ffresco.pricelist

  domain
    model
    enums
    exception

  application
    port
      in
      out
    service

  infrastructure
    adapter
      in
        api
        function
      out
        memory
        dynamodb
    config
```

## Responsibilities

| Layer/class type | Responsibility | Must not know |
|---|---|---|
| `RouteHandler` | API Gateway route, path/query/body extraction | Domain persistence details |
| `JsonApiMapper` | JSON:API serialization/deserialization | AWS deployment details |
| `Function` | Spring Cloud Function request/response adapter | API Gateway event shape |
| `UseCase` | Application action | JSON:API, AWS, Lambda |
| `Domain` | Business rules | Spring, Jackson, AWS |
| `Port out` | Needed external capability | Adapter implementation |
| `Adapter out` | DynamoDB/memory/external API implementation | API Gateway event shape |

## Wiring

Use explicit `@Configuration` + `@Bean` classes:

```text
ApiGatewayConfig
HealthConfig
PriceListConfig
PersistenceConfig
JacksonConfig
```

This keeps classes pure and makes the hexagonal wiring visible.

## OpenAPI and SAM

Recommended source of truth:

```text
openapi.yaml = API contract
template.yaml = infrastructure and Lambda deployment
```

`template.yaml` should reference `openapi.yaml` through `DefinitionUri` and expose the Lambda through API Gateway events.
