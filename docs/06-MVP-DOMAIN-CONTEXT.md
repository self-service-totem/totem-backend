# MVP Domain Context - Digital Menu / Ordering

This document is product/domain context. It is not required for every backend coding task, but it is useful when designing endpoints, DynamoDB access patterns or use cases.

## MVP decisions

```text
ADR-001: Use one physical Lambda for MVP-1.
ADR-002: Use DynamoDB single-table design.
ADR-003: Cart lives in frontend until order confirmation.
ADR-004: Online payment is outside MVP-1.
ADR-005: Kitchen screen uses polling in MVP-1.
```

## Main modules

```text
menu
table-session
order
account
kitchen
admin
```

## Essential MVP use cases

```text
GetMenuUseCase
OpenTableSessionUseCase
CreateOrderUseCase
GetTableAccountUseCase
GetCustomerAccountUseCase
RequestCustomerCloseUseCase
GetKitchenOrdersUseCase
UpdateOrderStatusUseCase
```

## Customer flow

```text
QR scan
  -> digital menu
  -> frontend cart
  -> create order
  -> kitchen display
  -> order status updates
  -> customer/table account
  -> close request
  -> manual payment by staff
```

## Table session concept

```text
TableSession
  -> CustomerSession Fernando
  -> CustomerSession Junior
  -> CustomerSession Mariana
```

Each order belongs to a customer session. Table account is the sum of customer accounts.

## DynamoDB base

Suggested main table:

```text
TotemTable
PK
SK
GSI1PK
GSI1SK
entityType
tenantId
branchId
status
createdAt
updatedAt
```

Main access patterns:

```text
Get menu by tenant/branch
Get products by category
Find active table session
Get table account
Get individual customer account
Get kitchen orders
Get pending kitchen orders
Request customer close
```

## Important MVP simplifications

```text
No persisted cart before order creation.
No online payment.
No WebSocket/AppSync initially.
Use polling for kitchen.
Internal domain events can exist as classes before SNS/SQS/EventBridge integration.
```
