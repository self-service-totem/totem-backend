# Task: Implement one vertical backend feature PR

## Context

This project is the FFresco backend.

The backend already has:
- Java 21
- Spring Cloud Function
- AWS Lambda
- API Gateway HTTP API
- SAM template
- DynamoDB single-table design
- Existing router that receives API Gateway events
- Existing pattern where the router delegates to a Spring Function
- Spring Function delegates to an application use case
- Use case orchestrates business logic and calls ports
- Output ports are implemented by DynamoDB adapters
- GitHub Actions deployment is already working

Do not rewrite the architecture.
Do not create a new framework.
Do not move unrelated files.
Do not implement endpoints not requested in this task.
Do not mix business logic inside the router.
Do not call DynamoDB directly from the router or from controllers.

---

## Future domain separation

Today everything is deployed inside one Lambda.

However, code must be organized as if each domain could become a separated Lambda later.

Each PR must preserve:
- one package grouping for the domain
- one path grouping in API Gateway
- clear separation between router/function/use case/ports/adapters/tests

Future domains:

```txt
/public/**      -> public/customer-api      -> com.ffresco.publicapi
/operations/**  -> operations/kitchen-api   -> com.ffresco.operations
/admin/**       -> admin/restaurant-api     -> com.ffresco.admin
/admin/menus/** -> catalog-api              -> com.ffresco.catalog
/admin/price-lists/** -> pricing-api        -> com.ffresco.pricing
/admin/branches/{branchId}/stock/** -> stock-api -> com.ffresco.stock
/payments/**    -> payments-api             -> com.ffresco.payments
```

### Domain package per domain
```txt
com.ffresco.totem.publicapi.menu
com.ffresco.totem.publicapi.order
com.ffresco.totem.operations.orders
com.ffresco.totem.admin.bootstrap
com.ffresco.totem.catalog.product
com.ffresco.totem.pricing.pricelist
com.ffresco.totem.stock.adjustment
com.ffresco.totem.payments.intent
com.ffresco.totem.notifications
```

