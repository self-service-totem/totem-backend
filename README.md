# Totem Backend Price List Lambda

Base project for an AWS Lambda using Spring Cloud Function, Java 21, AWS SAM, API Gateway HTTP API, JSON:API responses and Hexagonal Architecture.

## Current status

- AWS Lambda is deployed by SAM.
- API Gateway HTTP API is deployed by SAM.
- AWS execution uses one generic Spring Cloud Function router:

```text
apiGatewayRouter
```

- Local direct testing uses the function selected in `src/main/resources/application.yml`.
- `application.yml` was intentionally left as-is for local testing.
- Full architecture and naming rules are documented in:

```text
docs/architecture.md
```

## Active routes

```text
GET /health
GET /price-lists/{priceListId}
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
        pricelist
          GetPriceListUseCase.java
          GetPriceListCommand.java
      out
        LoadProductsPort.java
    service
      pricelist
        GetPriceListService.java

  infrastructure
    adapter
      in
        api
          ApiGatewayRouterFunction.java
          ApiGatewayRouteHandler.java
          ApiGatewayRequest.java
          ApiExceptionHandler.java
          JsonApiDocument.java
          JsonApiResource.java
          JsonApiError.java
          JsonApiResponseFactory.java
          health
            GetHealthRouteHandler.java
            HealthJsonApiMapper.java
            HealthAttributes.java
          pricelist
            GetPriceListRouteHandler.java
            PriceListJsonApiMapper.java
            PriceListAttributes.java
            PriceListProductAttributes.java
        function
          health
            HealthFunction.java
            HealthFunctionResponse.java
          pricelist
            GetPriceListFunction.java
            GetPriceListRequest.java
            GetPriceListResponse.java
            ProductResponse.java
      out
        memory
          InMemoryProductAdapter.java

    config
      ApiGatewayConfig.java
      HealthConfig.java
      PriceListConfig.java
      PersistenceConfig.java
      JacksonConfig.java
```

## Mental model

### Local direct test

```text
POST /health or POST /listPrice
  -> selected Spring Cloud Function bean from application.yml
  -> Function adapter
  -> UseCase
  -> Service
  -> Port out
  -> Adapter out
```

### AWS/API Gateway test

```text
GET /price-lists/{priceListId}
  -> API Gateway HTTP API v2 event
  -> Lambda
  -> apiGatewayRouter bean
  -> ApiGatewayRouterFunction
  -> GetPriceListRouteHandler
  -> PriceListJsonApiMapper
  -> GetPriceListFunction
  -> GetPriceListUseCase
  -> GetPriceListService
  -> LoadProductsPort
  -> InMemoryProductAdapter
```

## Important architecture rule

```text
JSON:API lives only in infrastructure.adapter.in.api.
Spring Cloud Function contracts live only in infrastructure.adapter.in.function.
Application and domain do not know JSON:API, API Gateway, Lambda or Spring Cloud Function.
```

## Requirements

- Java 21
- Maven 3.9+
- AWS CLI configured
- SAM CLI installed

Optional:

- Docker, only if you want to use `sam local`.

## Run unit tests

```bash
mvn clean test
```

## Run locally as simple HTTP function

This uses `application.yml`, where the current default function is:

```yaml
spring:
  cloud:
    function:
      definition: health
```

Run:

```bash
mvn -Plocal spring-boot:run
```

In another terminal:

```bash
curl -X POST "http://localhost:8080/health"
```

Expected health local/direct response:

```json
{
  "status": "ok"
}
```

To test the direct price-list function locally, temporarily set:

```yaml
spring:
  cloud:
    function:
      definition: listPrice
```

Then run:

```bash
curl -X POST "http://localhost:8080/listPrice" \
  -H "Content-Type: application/json" \
  -d '{"priceListId":"default"}'
```

## Build AWS Lambda artifact

```bash
mvn clean package
```

The generated AWS Lambda JAR is:

```text
target/price-list-lambda-0.0.1-SNAPSHOT-aws.jar
```

## Deploy with SAM

First time:

```bash
sam deploy --guided
```

Next deployments:

```bash
mvn clean package
sam deploy
```

## AWS/API Gateway response example

```bash
curl -i https://YOUR_API_ID.execute-api.sa-east-1.amazonaws.com/dev/price-lists/default
```

Expected AWS/API response in JSON:API format:

```json
{
  "data": {
    "type": "price-lists",
    "id": "default",
    "attributes": {
      "products": [
        {
          "id": "P-001",
          "name": "Café Especial 500g",
          "price": 12.9,
          "currency": "USD"
        }
      ]
    }
  }
}
```

## Lambda direct test event

When testing the Lambda directly from the AWS Lambda console, use an API Gateway HTTP API v2 event, not the simple local payload.

```json
{
  "version": "2.0",
  "routeKey": "GET /price-lists/{priceListId}",
  "rawPath": "/price-lists/default",
  "rawQueryString": "",
  "requestContext": {
    "http": {
      "method": "GET",
      "path": "/dev/price-lists/default"
    },
    "routeKey": "GET /price-lists/{priceListId}",
    "stage": "dev"
  },
  "pathParameters": {
    "priceListId": "default"
  },
  "isBase64Encoded": false
}
```

## How to add a new route

Read:

```text
docs/architecture.md
docs/07-add-new-route-checklist.md
```

Checklist:

```text
1. Create RouteHandler in infrastructure.adapter.in.api.<resource>.
2. Create JsonApiMapper and Attributes in the same api package.
3. Create Function + Request/Response in infrastructure.adapter.in.function.<resource>.
4. Create UseCase + Command in application.port.in.<resource>.
5. Create Service in application.service.<resource>.
6. Create/use output ports if needed.
7. Register beans in <Resource>Config.
8. Add route in template.yaml / OpenAPI.
9. Test locally direct or through an API Gateway event.
```

## AWS Lambda handler

```text
org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
```

## Active Spring Cloud Function in AWS

```yaml
SPRING_CLOUD_FUNCTION_DEFINITION: apiGatewayRouter
```

## OpenAPI

Current OpenAPI contract lives here:

```text
docs/openapi.yaml
```

For now, SAM creates API Gateway routes. OpenAPI is documentation/contract. Later it can become the source of truth for API Gateway creation.
