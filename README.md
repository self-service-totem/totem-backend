# Totem Backend Price List Lambda

Base project for an AWS Lambda using Spring Cloud Function, Java 21, AWS SAM, API Gateway HTTP API, JSON:API responses and Hexagonal Architecture.

## Current status

- AWS Lambda is deployed by SAM.
- API Gateway HTTP API is deployed by SAM.
- API Gateway route works remotely:

```text
GET /dev/price-lists/{priceListId}
```

- Local fast testing uses Spring Cloud Function Web:

```text
POST /listPrice
```

- AWS execution uses one generic API Gateway router:

```text
apiGatewayRouter
```

## Package map

```text
com.ffresco.pricelist
  application
    port
      in
        GetPriceListUseCase.java
        GetPriceListCommand.java
      out
        LoadProductsPort.java
    service
      GetPriceListService.java

  domain
    enums
      Currency.java
    model
      Money.java
      Product.java
      PriceList.java

  infrastructure
    adapter
      in
        api
          ApiGatewayRouterFunction.java
          ApiGatewayRouteHandler.java
          ApiGatewayRequest.java
          JsonApiResponseFactory.java
          JsonApiResource.java
          JsonApiError.java
          pricelist
            GetPriceListRouteHandler.java
            PriceListResource.java
            PriceListAttributes.java
        function
          ListPriceFunction.java
          ListPriceRequest.java
          ListPriceResponse.java
          ProductResponse.java
      out
        memory
          InMemoryProductAdapter.java
    config
      ApplicationConfig.java
      JacksonConfig.java
```

## Mental model

```text
Local fast test
  POST /listPrice
    -> ListPriceFunction
    -> GetPriceListUseCase
    -> GetPriceListService
    -> LoadProductsPort
    -> InMemoryProductAdapter
```

```text
AWS HTTP API
  GET /price-lists/{priceListId}
    -> API Gateway HTTP API v2 event
    -> Lambda
    -> apiGatewayRouter bean
    -> ApiGatewayRouterFunction
    -> GetPriceListRouteHandler
    -> ListPriceFunction
    -> GetPriceListUseCase
    -> GetPriceListService
    -> LoadProductsPort
    -> InMemoryProductAdapter
```

## Why there is an API router

The project avoids creating one full API Gateway adapter per endpoint.

Instead, API Gateway details are centralized in:

```text
ApiGatewayRouterFunction
JsonApiResponseFactory
ApiGatewayRequest
```

Each new endpoint only creates a small route handler:

```text
GetPriceListRouteHandler
CreateProductRouteHandler
UpdateProductRouteHandler
```

This keeps the framework reusable and reduces boilerplate.

## Requirements

- Java 21
- Maven 3.9+
- AWS CLI configured
- SAM CLI installed

Optional:

- Docker, only if you want to use `sam local`. This project does not depend on SAM local for daily development.

Check versions:

```bash
java -version
mvn -version
sam --version
aws --version
```

## Run unit tests

```bash
mvn clean test
```

## Run locally as simple HTTP function

This uses `application.yml`, where the default function is:

```yaml
spring:
  cloud:
    function:
      definition: listPrice
```

Run:

```bash
mvn -Plocal spring-boot:run
```

In another terminal:

```bash
curl -X POST "http://localhost:8080/listPrice" \
  -H "Content-Type: application/json" \
  -d '{"priceListId":"default"}'
```

Expected local/direct response:

```json
{
  "priceListId": "default",
  "products": [
    {
      "id": "P-001",
      "name": "Café Especial 500g",
      "price": 12.90,
      "currency": "USD"
    },
    {
      "id": "P-002",
      "name": "Yerba Mate Premium 1kg",
      "price": 8.50,
      "currency": "USD"
    }
  ]
}
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

Recommended values:

```text
Stack Name: price-list-lambda-stack
AWS Region: sa-east-1
Confirm changes before deploy: Y
Allow SAM CLI IAM role creation: Y
Disable rollback: N
Save arguments to samconfig.toml: Y
```

Next deployments:

```bash
mvn clean package
sam deploy
```

If you changed the template and want to be explicit:

```bash
mvn clean package
sam build --cached=false
sam deploy
```

## API Gateway route created by SAM

```text
GET /price-lists/{priceListId}
```

Example:

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

Use:

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
docs/07-add-new-route-checklist.md
```

Short version:

```text
1. Create use case command/interface/service.
2. Create output port/adapter if needed.
3. Create local/direct function if useful.
4. Create RouteHandler.
5. Create JSON:API resource mapper.
6. Register beans in ApplicationConfig.
7. Add HttpApi event in template.yaml.
8. Deploy and test with Postman/curl.
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

## Knowledge base files

Reusable MD files under `docs/`:

```text
docs/01-architecture-map.md
docs/02-add-new-function-checklist.md
docs/03-local-vs-aws-testing.md
docs/04-troubleshooting.md
docs/05-chatgpt-context.md
docs/06-api-gateway-router-framework.md
docs/07-add-new-route-checklist.md
docs/08-json-api-first.md
docs/09-openapi-roadmap.md
docs/10-project-generator-idea.md
```

Use `docs/05-chatgpt-context.md` and `docs/06-api-gateway-router-framework.md` as the base context when asking ChatGPT to add new backend functions.
