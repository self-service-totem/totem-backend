# Architecture Map - Spring Cloud Function + Lambda + API Gateway

## Goal

This project is a base template for backend functions using:

- Java 21
- Spring Cloud Function
- AWS Lambda
- API Gateway HTTP API v2
- SAM CLI
- Hexagonal Architecture

## Package structure

```text
com.ffresco.pricelist
  application
    port
      in        # use case interfaces and commands
      out       # output ports required by the use cases
    service     # use case implementations

  domain
    model       # business model
    enums       # domain enums

  infrastructure
    adapter
      in
        function # Lambda/Spring Cloud Function inbound adapters
      out
        memory   # temporary output adapter
        dynamo   # future DynamoDB adapter
    config       # Spring bean wiring
```

## Dependency direction

```text
infrastructure -> application -> domain
```

The domain does not depend on Spring, Lambda, API Gateway, DynamoDB, or AWS SDK.

## Runtime flows

### Local direct flow

```text
POST /listPrice
  -> listPrice bean
  -> ListPriceFunction
  -> GetPriceListUseCase
  -> GetPriceListService
  -> LoadProductsPort
  -> InMemoryProductAdapter
```

### AWS/API Gateway flow

```text
GET /price-lists/{priceListId}
  -> API Gateway HTTP API v2
  -> Lambda
  -> Spring Cloud Function FunctionInvoker
  -> listPriceApiGateway bean
  -> ListPriceApiGatewayAdapter
  -> ListPriceFunction
  -> GetPriceListUseCase
  -> GetPriceListService
  -> LoadProductsPort
  -> InMemoryProductAdapter
```

## Why there are two inbound functions

`listPrice` exists to test locally with a simple request body.

`listPriceApiGateway` exists for AWS/API Gateway because API Gateway sends a bigger event object: `APIGatewayV2HTTPEvent`.

This prevents the use case and the simple function from being polluted with API Gateway details.
