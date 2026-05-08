# Checklist: Add a New Route

Use this when adding a new path/method to the backend.

## Example

New endpoint:

```text
GET /products/{productId}
```

## 1. Application layer

Create command:

```text
src/main/java/.../application/port/in/GetProductCommand.java
```

Create input port:

```text
src/main/java/.../application/port/in/GetProductUseCase.java
```

Create service:

```text
src/main/java/.../application/service/GetProductService.java
```

Create output port if needed:

```text
src/main/java/.../application/port/out/LoadProductPort.java
```

## 2. Domain layer

Reuse or create domain models in:

```text
src/main/java/.../domain/model
src/main/java/.../domain/enums
```

Do not add Spring or AWS dependencies in domain.

## 3. Output adapter

For the first MVP, use memory:

```text
src/main/java/.../infrastructure/adapter/out/memory
```

Later replace/add DynamoDB adapter:

```text
src/main/java/.../infrastructure/adapter/out/dynamo
```

## 4. Local/direct function

Create a simple function if you want fast local testing with Spring Cloud Function Web:

```text
infrastructure.adapter.in.function.GetProductFunction
```

This function should receive a simple request object and return a simple response object.

Example local call:

```bash
curl -X POST http://localhost:8080/getProduct \
  -H "Content-Type: application/json" \
  -d '{"productId":"P-001"}'
```

## 5. API route handler

Create a route handler:

```text
infrastructure.adapter.in.api.product.GetProductRouteHandler
```

It must implement:

```java
ApiGatewayRouteHandler
```

It must return the exact API Gateway route key:

```java
@Override
public String routeKey() {
    return "GET /products/{productId}";
}
```

## 6. JSON:API resource

Create a resource mapper:

```text
ProductResource.java
ProductAttributes.java
```

Expected response style:

```json
{
  "data": {
    "type": "products",
    "id": "P-001",
    "attributes": {
      "name": "Example",
      "price": 10.5,
      "currency": "USD"
    }
  }
}
```

## 7. Register beans

In `ApplicationConfig`, register:

- use case service
- local/direct function if needed
- route handler

You do not need to create a new API Gateway adapter. The shared `apiGatewayRouter` handles API Gateway dispatching.

## 8. Add route to SAM template

Add a new event under `PriceListFunction.Events`:

```yaml
GetProduct:
  Type: HttpApi
  Properties:
    ApiId: !Ref PriceListHttpApi
    Path: /products/{productId}
    Method: GET
    PayloadFormatVersion: '2.0'
```

The Lambda remains the same. The router dispatches using `routeKey`.

## 9. Test locally fast

```bash
mvn -Plocal spring-boot:run
```

Then call the local/simple function endpoint.

## 10. Deploy and test remotely

```bash
mvn clean package
sam deploy
```

Then call the real API Gateway endpoint with Postman or curl.
