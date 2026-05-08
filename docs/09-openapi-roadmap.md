# OpenAPI Roadmap

## Current stage

SAM owns the API Gateway routes through function events:

```yaml
Events:
  GetPriceList:
    Type: HttpApi
    Properties:
      ApiId: !Ref PriceListHttpApi
      Path: /price-lists/{priceListId}
      Method: GET
      PayloadFormatVersion: '2.0'
```

This is the fastest and least fragile option while endpoints are still changing.

## Recommended next stage

Maintain an OpenAPI file as documentation/contract:

```text
docs/openapi.yaml
```

Use it to keep endpoint names, path params, request/response examples and JSON:API payloads consistent.

## Later stage

When the API stabilizes, OpenAPI can become the source of truth for API Gateway.

That requires using API Gateway extensions like:

```text
x-amazon-apigateway-integration
```

That is powerful, but more verbose. Do it after the first 3-5 endpoints are stable.
