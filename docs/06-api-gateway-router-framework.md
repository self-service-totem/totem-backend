# API Gateway Router Framework

## Decision

This project uses one AWS Lambda entrypoint per backend module and an internal API Gateway router.

For the price-list module, AWS invokes this Spring Cloud Function bean:

```yaml
SPRING_CLOUD_FUNCTION_DEFINITION: apiGatewayRouter
```

The router receives `APIGatewayV2HTTPEvent`, reads `routeKey`, and delegates to the matching `ApiGatewayRouteHandler`.

## Runtime flow

```text
API Gateway HTTP API
  GET /price-lists/{priceListId}
        ↓
AWS Lambda Java 21
        ↓
Spring Cloud Function FunctionInvoker
        ↓
apiGatewayRouter
        ↓
routeKey = GET /price-lists/{priceListId}
        ↓
GetPriceListRouteHandler
        ↓
ListPriceFunction
        ↓
GetPriceListUseCase
        ↓
GetPriceListService
        ↓
LoadProductsPort
        ↓
InMemoryProductAdapter / Dynamo adapter later
```

## Why this pattern

The repeated API Gateway concerns are centralized:

- reading `routeKey`
- returning 404 when a route has no handler
- returning JSON:API errors
- building `APIGatewayV2HTTPResponse`
- setting `Content-Type: application/vnd.api+json`

Each endpoint only needs a small `RouteHandler`.

## Core classes

```text
infrastructure.adapter.in.api
  ApiGatewayRouterFunction.java
  ApiGatewayRouteHandler.java
  ApiGatewayRequest.java
  JsonApiResponseFactory.java
  JsonApiResource.java
  JsonApiError.java
```

Current route:

```text
infrastructure.adapter.in.api.pricelist
  GetPriceListRouteHandler.java
  PriceListResource.java
  PriceListAttributes.java
```

Local/simple function:

```text
infrastructure.adapter.in.function
  ListPriceFunction.java
  ListPriceRequest.java
  ListPriceResponse.java
```

The local function does not know about API Gateway.
