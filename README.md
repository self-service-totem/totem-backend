# Price List Lambda

Base project for an AWS Lambda using Spring Cloud Function, Java 21 and Hexagonal Architecture.

## Architecture

```text
com.example.pricelist
  application
    port
      in
      out
    service
  domain
    enums
    model
  infrastructure
    adapter
      in
        function
      out
        memory
    config
```

## Main flow

```text
AWS Lambda / local invocation
  -> infrastructure.adapter.in.function.ListPriceFunction
  -> application.port.in.GetPriceListUseCase
  -> application.service.GetPriceListService
  -> application.port.out.LoadProductsPort
  -> infrastructure.adapter.out.memory.InMemoryProductAdapter
  -> domain.model.PriceList
```

## Function name

```text
listPrice
```

## AWS Lambda handler

```text
org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
```

## Local requirements

- Java 21
- Maven 3.9+

Check versions:

```bash
java -version
mvn -version
```

## Run tests

```bash
mvn clean test
```

## Build AWS Lambda artifact

```bash
mvn clean package
```

The generated AWS Lambda jar will be:

```text
target/price-list-lambda-0.0.1-SNAPSHOT-aws.jar
```

## Run locally as HTTP with Spring Cloud Function Web

```bash
mvn spring-boot:run
```

In another terminal:

```bash
curl -X POST "http://localhost:8080/listPrice" \
  -H "Content-Type: application/json" \
  -d '{"priceListId":"default"}'
```

## Optional: invoke locally using the AWS Lambda Runtime Interface Emulator

For a Lambda-like local execution, use the AWS Lambda Runtime Interface Emulator together with the generated `*-aws.jar`. The handler remains:

```text
org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
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

## Deploy note

When creating the Lambda in AWS:

- Runtime: Java 21
- Handler: `org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest`
- Artifact: `target/price-list-lambda-0.0.1-SNAPSHOT-aws.jar`
