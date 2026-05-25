# Local vs AWS Testing

## Local direct testing

Use this for fast business-flow testing without API Gateway event payloads.

```bash
mvn -Plocal spring-boot:run
```

Example direct function call:

```bash
curl -X POST "http://localhost:8080/listPrice" \
  -H "Content-Type: application/json" \
  -d '{"priceListId":"default"}'
```

This depends on `spring.cloud.function.definition` in `application.yml`. Keep `application.yml` as the local/direct testing config.

## SAM / API Gateway local simulation

Use this when you want to validate the AWS event shape.

```bash
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json
```

AWS/SAM should use:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

## Why both exist

Local direct receives a simple function request:

```json
{
  "priceListId": "default"
}
```

API Gateway mode receives an AWS event:

```json
{
  "version": "2.0",
  "routeKey": "GET /price-lists/{priceListId}",
  "rawPath": "/price-lists/default",
  "pathParameters": {
    "priceListId": "default"
  },
  "requestContext": {
    "http": {
      "method": "GET",
      "path": "/price-lists/default"
    }
  }
}
```

## Common rebuild

```bash
rm -rf .aws-sam
mvn clean package
sam build --cached=false
```
