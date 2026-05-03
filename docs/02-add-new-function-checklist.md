# Checklist - Add a New Backend Function

Use this checklist every time a new backend operation is added.

Example operation: `createProduct`.

## 1. Application input port

Create:

```text
application/port/in/CreateProductUseCase.java
application/port/in/CreateProductCommand.java
```

The use case interface represents what the application can do.

## 2. Application service

Create:

```text
application/service/CreateProductService.java
```

This class implements the use case and orchestrates domain objects and output ports.

## 3. Output port if needed

Create only if the use case needs persistence, external APIs, queues, or events.

```text
application/port/out/SaveProductPort.java
```

## 4. Output adapter

Create an implementation in infrastructure.

```text
infrastructure/adapter/out/memory/InMemoryProductAdapter.java
infrastructure/adapter/out/dynamo/DynamoProductAdapter.java
```

## 5. Inbound function request/response

Create DTOs for the function boundary.

```text
infrastructure/adapter/in/function/createproduct/CreateProductRequest.java
infrastructure/adapter/in/function/createproduct/CreateProductResponse.java
```

## 6. Local/direct function

Create a simple function that receives a simple request and returns a simple response.

```text
CreateProductFunction implements Function<CreateProductRequest, CreateProductResponse>
```

This function must not know about API Gateway.

## 7. API Gateway adapter

Create an API Gateway-specific adapter only when the function is exposed by HTTP API.

```text
CreateProductApiGatewayAdapter implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>
```

This class extracts path params, query params, headers, and body. It then calls the local/direct function or the use case.

## 8. Register beans

Update:

```text
infrastructure/config/ApplicationConfig.java
```

Add beans for:

- use case service
- local/direct function
- api gateway adapter function

## 9. Add route to SAM

Update `template.yaml`:

```yaml
Events:
  CreateProduct:
    Type: HttpApi
    Properties:
      ApiId: !Ref PriceListHttpApi
      Path: /products
      Method: POST
      PayloadFormatVersion: '2.0'
```

## 10. Add local events

Create:

```text
events/create-product-local.json
events/api-gateway-create-product.json
```

## 11. Test

```bash
mvn clean test
mvn -Plocal spring-boot:run
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-create-product.json
```
