# Backlog Ideas

## Project generator

Future idea: create a generator that receives module name, package name and routes, then outputs a ready-to-run Java 21 + Spring Cloud Function + SAM + API Gateway + JSON:API + hexagonal architecture project.

Input example:

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

Output:

```text
Maven project
Spring Cloud Function setup
SAM template
OpenAPI contract
API Gateway route
router framework
JSON:API response factory
sample use case
sample memory adapter
docs
sample events
```
