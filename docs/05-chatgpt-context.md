# ChatGPT Context - Backend Function Template

Use this file as context when asking ChatGPT to create or modify backend functions in this project.

## Project style

- Java 21
- Spring Cloud Function
- AWS Lambda
- API Gateway HTTP API v2
- SAM CLI
- Hexagonal Architecture

## Package rules

- `domain` must not depend on Spring or AWS.
- `application` should ideally not depend on Spring or AWS.
- `infrastructure` can depend on Spring, AWS Lambda events, SAM, DynamoDB, etc.

## Ports

- `application.port.in`: use case interfaces and commands.
- `application.port.out`: output ports required by use cases.

## Adapters

- `infrastructure.adapter.in.function`: Spring Cloud Function / Lambda inbound adapters.
- `infrastructure.adapter.out.*`: persistence, APIs, queues, and external systems.

## Two inbound function styles

For each HTTP-exposed operation, prefer two layers:

1. Local/direct function:

```text
Function<SimpleRequest, SimpleResponse>
```

2. API Gateway adapter:

```text
Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>
```

The API Gateway adapter extracts path/query/body and delegates to the local/direct function or use case.

## Existing function names

- `listPrice`: local/direct function.
- `listPriceApiGateway`: AWS/API Gateway function.

## SAM rules

- `template.yaml` must use Java 21.
- Handler must be:

```text
org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
```

- AWS/SAM must set:

```yaml
SPRING_CLOUD_FUNCTION_DEFINITION: listPriceApiGateway
SPRING_MAIN_WEB_APPLICATION_TYPE: none
```

## Local direct command

```bash
mvn -Plocal spring-boot:run
```

```bash
curl -X POST "http://localhost:8080/listPrice" \
  -H "Content-Type: application/json" \
  -d '{"priceListId":"default"}'
```

## SAM local command

```bash
rm -rf .aws-sam
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json
```
