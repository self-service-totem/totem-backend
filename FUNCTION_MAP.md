# Function map

## Base pattern

```text
API Gateway route
  -> API Gateway adapter function
  -> Local/direct function
  -> Use case interface
  -> Use case service
  -> Output port
  -> Output adapter
```

## Current function

```text
GET /price-lists/{priceListId}
  -> listPriceApiGateway
  -> ListPriceApiGatewayAdapter
  -> listPrice
  -> ListPriceFunction
  -> GetPriceListUseCase
  -> GetPriceListService
  -> LoadProductsPort
  -> InMemoryProductAdapter
```

## Local/direct testing

```text
POST /listPrice
  body: { "priceListId": "default" }
```

## AWS/API Gateway testing

```text
GET /price-lists/default
```

## Add a new function

Example: `createProduct`.

```text
1. application.port.in
   CreateProductCommand
   CreateProductUseCase

2. application.service
   CreateProductService

3. application.port.out
   SaveProductPort

4. infrastructure.adapter.out
   DynamoProductAdapter or InMemoryProductAdapter

5. infrastructure.adapter.in.function
   CreateProductFunction
   CreateProductApiGatewayAdapter
   CreateProductRequest
   CreateProductResponse

6. infrastructure.config.ApplicationConfig
   @Bean createProduct
   @Bean createProductApiGateway

7. template.yaml
   POST /products -> createProductApiGateway
```
