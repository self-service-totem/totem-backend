# Local vs AWS Testing

## Local direct test

Use this when you want to test the business flow without API Gateway.

```bash
mvn -Plocal spring-boot:run
```

Then:

```bash
curl -X POST "http://localhost:8080/listPrice" \
  -H "Content-Type: application/json" \
  -d '{"priceListId":"default"}'
```

This uses:

```yaml
spring:
  cloud:
    function:
      definition: listPrice
```

## Lambda/API Gateway local simulation

Use this when you want to test the AWS event shape.

```bash
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json
```

This uses the environment variable from `template.yaml`:

```yaml
SPRING_CLOUD_FUNCTION_DEFINITION: listPriceApiGateway
```

## Why they are different

Local direct mode receives:

```json
{
  "priceListId": "default"
}
```

API Gateway mode receives:

```json
{
  "version": "2.0",
  "routeKey": "GET /price-lists/{priceListId}",
  "pathParameters": {
    "priceListId": "default"
  }
}
```

## Common fix after changing code

Always rebuild the JAR before SAM local invoke:

```bash
rm -rf .aws-sam
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json
```
