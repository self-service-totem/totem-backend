# Future Task: Lambda Project Generator

Goal: create a small internal generator that receives a module name and a few specifications and outputs a ready-to-run ZIP project.

## Input example

```yaml
moduleName: price-list
packageName: com.ffresco.pricelist
runtime: java21
architecture: hexagonal
apiStyle: json-api
routes:
  - method: GET
    path: /price-lists/{priceListId}
    useCase: GetPriceList
```

## Output

A ZIP containing:

- Maven project
- Spring Cloud Function setup
- AWS Lambda SAM template
- API Gateway HTTP API route
- router framework
- JSON:API response factory
- sample use case
- sample memory adapter
- README
- events for Lambda console testing
- docs for adding new routes

## Why

This allows fast creation of new backend modules without repeating setup work.
