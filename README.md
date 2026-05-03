# Totem Backend Price List Lambda

Base project for an AWS Lambda using Spring Cloud Function, Java 21, AWS SAM, API Gateway HTTP API and Hexagonal Architecture.

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
        function
          ListPriceFunction.java              # local/direct function
          ListPriceApiGatewayAdapter.java    # AWS API Gateway adapter
          ApiGatewayResponseFactory.java     # common HTTP response builder
          ListPriceRequest.java
          ListPriceResponse.java
          ProductResponse.java
      out
        memory
          InMemoryProductAdapter.java
    config
      ApplicationConfig.java
```

## Mental model

```text
Local direct test
  POST /listPrice
    -> ListPriceFunction
    -> GetPriceListUseCase
    -> GetPriceListService
    -> LoadProductsPort
    -> InMemoryProductAdapter

AWS HTTP API
  GET /price-lists/{priceListId}
    -> API Gateway HTTP API v2 event
    -> Lambda
    -> listPriceApiGateway bean
    -> ListPriceApiGatewayAdapter
    -> ListPriceFunction
    -> GetPriceListUseCase
    -> GetPriceListService
    -> LoadProductsPort
    -> InMemoryProductAdapter
```

## Why there are two functions

The project has two inbound functions on purpose:

```text
listPrice
```

Receives a simple JSON request and is useful for local development:

```json
{
  "priceListId": "default"
}
```

```text
listPriceApiGateway
```

Receives the real `APIGatewayV2HTTPEvent` sent by API Gateway HTTP API v2 and returns an `APIGatewayV2HTTPResponse`.

This keeps the business flow clean. API Gateway details stay inside `infrastructure.adapter.in.function.ListPriceApiGatewayAdapter`.

## Requirements

- Java 21
- Maven 3.9+
- AWS CLI configured
- SAM CLI installed
- Docker installed if you want to use `sam local invoke`

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

Expected response:

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

## Test API Gateway event locally with SAM

The SAM template overrides the function definition for AWS:

```yaml
SPRING_CLOUD_FUNCTION_DEFINITION: listPriceApiGateway
```

Invoke locally with the API Gateway v2 event:

```bash
sam build
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json
```

This simulates:

```http
GET /price-lists/default
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
sam build
sam deploy
```

## API Gateway route created by SAM

```text
GET /price-lists/{priceListId}
```

Example:

```bash
curl https://YOUR_API_ID.execute-api.sa-east-1.amazonaws.com/dev/price-lists/default
```

SAM also prints this URL as the output:

```text
PriceListApiUrl
```

## How to add a new operation

For each new operation, follow this map:

```text
1. application.port.in
   - Create command
   - Create use case interface

2. application.service
   - Create use case implementation

3. application.port.out
   - Create output port if the use case needs persistence, messaging or external APIs

4. infrastructure.adapter.out
   - Create adapter implementation, for example DynamoDB, SQS, SNS, memory

5. infrastructure.adapter.in.function
   - Create a local/direct function if you want simple local testing
   - Create an API Gateway adapter if the operation is exposed by HTTP

6. infrastructure.config
   - Register beans

7. template.yaml
   - Add the API Gateway route/event
```

Recommended naming example:

```text
GET /price-lists/{priceListId}
  listPrice                  # local/direct function
  listPriceApiGateway        # AWS/API Gateway function

POST /products
  createProduct              # local/direct function
  createProductApiGateway    # AWS/API Gateway function
```

## AWS Lambda handler

```text
org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
```
