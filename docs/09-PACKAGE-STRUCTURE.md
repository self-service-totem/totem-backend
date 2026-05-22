# Package Structure

The project is organized as a modular monolith inside one Lambda.

The main rule is:

```text
Business capability first, technical layer second.
```

That means each capability is a small hexagonal architecture:

```text
com.ffresco.totem
  catalog
    domain
    application
    infrastructure

  pricelist
    domain
    application
    infrastructure

  common
    domain
    infrastructure
```

## Why this structure

The project starts as one Lambda for simplicity and low operational cost.

Later, if one capability needs to be deployed independently, the package can be extracted with less refactoring.

Example:

```text
catalog-lambda
  com.ffresco.totem.catalog
  com.ffresco.totem.common

ordering-lambda
  com.ffresco.totem.ordering
  com.ffresco.totem.common
```

## Current capabilities

### catalog

Owns catalog version behavior.

Examples:

- Current catalog version by branch
- Catalog version persistence
- Catalog refresh decisions

### pricelist

Owns price list behavior.

Examples:

- Price list retrieval
- Products exposed by price list
- Price data returned to clients

### common

Owns stable cross-cutting concepts and infrastructure helpers.

Examples:

- Shared value objects such as `Money`
- Shared enums such as `Currency`
- Shared domain exceptions
- JSON:API response helpers
- API Gateway router abstractions
- Health endpoint
- Common Spring configuration

## Common package rule

`common` must not become a garbage package.

Only put something in `common` when it is stable and truly reused by multiple capabilities.

Good candidates:

```text
Money
Currency
TenantId
BranchId
DomainException
JsonApiResponseFactory
ApiGatewayRouteHandler
```

Bad candidates:

```text
Product
Order
PriceList
StockReservation
Payment
KitchenOrder
```

Those belong to a business capability.
