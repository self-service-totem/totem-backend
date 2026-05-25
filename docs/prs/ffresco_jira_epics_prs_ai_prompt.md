# FFresco — Épicas Jira, PRs por dominio y prompt base para IA

## Objetivo

Este documento organiza el trabajo backend de FFresco en épicas de Jira basadas en dominios/futuras Lambdas.

La regla de trabajo será:

```txt
Epic = dominio / futura Lambda
Story o Task = feature endpoint o grupo chico de endpoints
PR = implementación vertical revisable
```

La implementación actual puede vivir en una sola Lambda, pero el código debe quedar separado por dominio para poder extraerlo mañana en Lambdas independientes.

---

# Épica 1 — Public / Customer API

**Objetivo:** endpoints públicos consumidos por menú digital, QR de mesa y tótem.

Path agrupador:

```http
/public/**
```

Paquete sugerido:

```txt
com.ffresco.publicapi
```

Futura Lambda:

```txt
ffresco-public-api
```

## PRs / Tasks

### PR 1 — Public Menu

```http
GET /public/menu?tableId={tablePublicId}
GET /public/menu/products/{productId}?tableId={tablePublicId}
```

Tasks:

- Crear paquete `publicapi.menu`
- Crear router/route mapping para `/public/menu`
- Crear Spring Function / handler interno
- Crear `GetPublicMenuUseCase`
- Crear port de lectura de mesa pública
- Crear port de lectura de menú público
- Crear adapter DynamoDB
- Crear DTOs de response
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 2 — Place Order

```http
POST /public/tables/{tablePublicId}/orders
```

Tasks:

- Crear paquete `publicapi.order`
- Crear router/route mapping
- Crear Spring Function / handler interno
- Crear `PlaceOrderUseCase`
- Resolver mesa por `tablePublicId`
- Buscar o crear `TableSession OPEN`
- Validar productos contra menú activo
- Calcular precios en backend
- Crear ítem `ORDER`
- Crear GSI para cocina por branch/status
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 3 — Customer Orders

```http
GET /public/tables/{tablePublicId}/orders
```

Tasks:

- Reutilizar paquete `publicapi.order`
- Crear `ListTableOrdersUseCase`
- Soportar filtro opcional `customerName`
- Leer órdenes de `TableSession OPEN`
- Mapear response para frontend customer
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 4 — Bill

```http
GET /public/tables/{tablePublicId}/bill
POST /public/tables/{tablePublicId}/bill/close-request
```

Tasks:

- Crear paquete `publicapi.bill`
- Crear `GetTableBillUseCase`
- Crear `RequestBillCloseUseCase`
- Calcular cuenta por cliente
- Calcular cuenta total de mesa
- Ignorar órdenes canceladas
- Soportar `scope = MINE | TABLE`
- Crear ítem `BILL_CLOSE_REQUEST`
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 5 — Waiter Calls

```http
POST /public/tables/{tablePublicId}/waiter-calls
```

Tasks:

- Crear paquete `publicapi.waiter`
- Crear `CreateWaiterCallUseCase`
- Validar `reason`
- Crear ítem `WAITER_CALL`
- Crear GSI para operaciones por branch/status
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

---

# Épica 2 — Operations / Kitchen API

**Objetivo:** endpoints internos para cocina, pantalla de pedidos, mozo/caja.

Path agrupador:

```http
/operations/**
```

Paquete sugerido:

```txt
com.ffresco.operations
```

Futura Lambda:

```txt
ffresco-operations-api
```

## PRs / Tasks

### PR 6 — Kitchen Orders List

```http
GET /operations/branches/{branchId}/orders
```

Tasks:

- Crear paquete `operations.orders`
- Crear router/route mapping
- Crear `ListKitchenOrdersUseCase`
- Leer órdenes por `branchId` y `status`
- Usar GSI por `BRANCH#{branchId}#ORDER_STATUS#{status}`
- Mapear response para KDS/cocina
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 7 — Update Order Status

```http
PATCH /operations/orders/{orderId}/status
```

Tasks:

- Crear `UpdateOrderStatusUseCase`
- Validar estado permitido
- Validar transición de estado
- Actualizar `status`
- Actualizar GSI de búsqueda por status
- Guardar `updatedAt`
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 8 — Order Display

```http
GET /operations/branches/{branchId}/order-display
```

Tasks:

- Crear paquete `operations.display`
- Crear `GetOrderDisplayUseCase`
- Leer pedidos `PREPARING` y `READY`
- Devolver columnas para pantalla pública
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 9 — Operations Waiter Calls

```http
GET /operations/branches/{branchId}/waiter-calls
PATCH /operations/waiter-calls/{waiterCallId}/status
```

Tasks:

- Crear paquete `operations.waiter`
- Crear `ListWaiterCallsUseCase`
- Crear `UpdateWaiterCallStatusUseCase`
- Leer llamados por branch/status
- Actualizar estado `OPEN | IN_PROGRESS | DONE | CANCELLED`
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 10 — Bill Close Requests

```http
GET /operations/branches/{branchId}/bill-close-requests
PATCH /operations/bill-close-requests/{requestId}/status
```

Tasks:

- Crear paquete `operations.bill`
- Crear `ListBillCloseRequestsUseCase`
- Crear `UpdateBillCloseRequestStatusUseCase`
- Leer solicitudes abiertas
- Actualizar estado de solicitud
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

---

# Épica 3 — Admin / Restaurant API

**Objetivo:** administración de tenant, sucursales, mesas y tótems.

Path agrupador:

```http
/admin/**
```

Paquete sugerido:

```txt
com.ffresco.admin.restaurant
```

Futura Lambda:

```txt
ffresco-admin-api
```

## PRs / Tasks

### PR 11 — Bootstrap Demo

```http
POST /admin/dev/bootstrap-demo
```

Tasks:

- Crear paquete `admin.bootstrap`
- Crear `BootstrapDemoUseCase`
- Crear tenant demo `Shop Test Demo`
- Crear branch `B001`
- Crear mesa `001`
- Crear tótem `Demo Totem`
- Crear menú demo
- Crear categorías demo
- Crear productos demo
- Crear lista de precios demo
- Publicar menú demo
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 12 — Tenant Management

```http
POST /admin/tenants
GET /admin/tenants/{tenantId}
```

Tasks:

- Crear paquete `admin.restaurant.tenant`
- Crear `CreateTenantUseCase`
- Crear `GetTenantUseCase`
- Validar slug único
- Persistir tenant
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 13 — Branch Management

```http
POST /admin/tenants/{tenantId}/branches
GET /admin/branches/{branchId}
```

Tasks:

- Crear paquete `admin.restaurant.branch`
- Crear `CreateBranchUseCase`
- Crear `GetBranchUseCase`
- Persistir branch asociada al tenant
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 14 — Tables and Kiosks

```http
POST /admin/branches/{branchId}/tables
GET /admin/branches/{branchId}/tables
PATCH /admin/tables/{tableId}
```

Tasks:

- Crear paquete `admin.restaurant.table`
- Crear `CreateTableUseCase`
- Crear `ListBranchTablesUseCase`
- Crear `UpdateTableUseCase`
- Soportar `type = TABLE | KIOSK`
- Generar `tablePublicId`
- Generar QR URL si es mesa
- No generar QR URL si es tótem
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

---

# Épica 4 — Catalog API

**Objetivo:** gestión de menú, categorías y productos.

Path agrupador actual:

```http
/admin/menus/**
/admin/products/**
/admin/categories/**
```

Paquete sugerido:

```txt
com.ffresco.catalog
```

Futura Lambda:

```txt
ffresco-catalog-api
```

## PRs / Tasks

### PR 15 — Menu Management

```http
POST /admin/branches/{branchId}/menus
GET /admin/branches/{branchId}/menus
POST /admin/branches/{branchId}/menus/{menuId}/publish
```

Tasks:

- Crear paquete `catalog.menu`
- Crear `CreateMenuUseCase`
- Crear `ListBranchMenusUseCase`
- Crear `PublishMenuUseCase`
- Publicar menú como `activeMenuId`
- Actualizar proyección `PUBLIC_MENU`
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 16 — Categories

```http
POST /admin/menus/{menuId}/categories
PATCH /admin/categories/{categoryId}
```

Tasks:

- Crear paquete `catalog.category`
- Crear `CreateCategoryUseCase`
- Crear `UpdateCategoryUseCase`
- Mantener orden de categorías
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 17 — Products

```http
POST /admin/menus/{menuId}/products
GET /admin/products/{productId}
PATCH /admin/products/{productId}
```

Tasks:

- Crear paquete `catalog.product`
- Crear `CreateProductUseCase`
- Crear `GetProductUseCase`
- Crear `UpdateProductUseCase`
- Soportar producto activo/inactivo
- Soportar producto disponible/no disponible
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

---

# Épica 5 — Pricing API

**Objetivo:** listas de precios, precios de venta, descuentos y activación.

Path agrupador:

```http
/admin/price-lists/**
```

Paquete sugerido:

```txt
com.ffresco.pricing
```

Futura Lambda:

```txt
ffresco-pricing-api
```

## PRs / Tasks

### PR 18 — Price Lists

```http
POST /admin/branches/{branchId}/price-lists
GET /admin/branches/{branchId}/price-lists
GET /admin/price-lists/{priceListId}
```

Tasks:

- Crear paquete `pricing.pricelist`
- Crear `CreatePriceListUseCase`
- Crear `ListPriceListsUseCase`
- Crear `GetPriceListUseCase`
- Soportar nombre, moneda y vigencia
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 19 — Price List Items

```http
POST /admin/price-lists/{priceListId}/items
PATCH /admin/price-lists/{priceListId}/items/{productId}
```

Tasks:

- Crear `AddPriceListItemUseCase`
- Crear `UpdatePriceListItemUseCase`
- Guardar precio de costo opcional
- Guardar precio de venta
- Validar precio mayor o igual a cero
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 20 — Discounts

```http
POST /admin/price-lists/{priceListId}/discounts
PATCH /admin/price-lists/{priceListId}/discounts/{discountId}
```

Tasks:

- Crear paquete `pricing.discount`
- Crear `CreateDiscountUseCase`
- Crear `UpdateDiscountUseCase`
- Soportar descuento por producto/categoría/lista
- Soportar porcentaje o monto fijo
- Validar vigencia
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 21 — Activate Price List

```http
POST /admin/branches/{branchId}/price-lists/{priceListId}/activate
```

Tasks:

- Crear `ActivatePriceListUseCase`
- Activar lista de precios en sucursal
- Regenerar proyección de menú público con precios vigentes
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

---


# Épica 6 — Stock API

**Objetivo:** stock por sucursal, movimientos, reservas y alertas.

Path agrupador:

```http
/admin/branches/{branchId}/stock/**
```

Paquete sugerido:

```txt
com.ffresco.stock
```

Futura Lambda:

```txt
ffresco-stock-api
```

## PRs / Tasks

### PR 22 — Stock Query

```http
GET /admin/branches/{branchId}/stock
GET /admin/branches/{branchId}/stock/{productId}
```

Tasks:

- Crear paquete `stock.query`
- Crear `ListStockUseCase`
- Crear `GetProductStockUseCase`
- Leer stock por branch/product
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 23 — Stock Update / Adjustment

```http
PATCH /admin/branches/{branchId}/stock/{productId}
POST /admin/branches/{branchId}/stock-adjustments
```

Tasks:

- Crear paquete `stock.adjustment`
- Crear `UpdateStockUseCase`
- Crear `CreateStockAdjustmentUseCase`
- Registrar movimiento de stock
- Validar cantidades
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 24 — Stock Movements and Alerts

```http
GET /admin/branches/{branchId}/stock-movements
GET /admin/branches/{branchId}/stock-alerts
```

Tasks:

- Crear paquete `stock.movement`
- Crear paquete `stock.alert`
- Crear `ListStockMovementsUseCase`
- Crear `ListStockAlertsUseCase`
- Calcular productos bajo mínimo
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 25 — Stock Reservations

```http
POST /admin/branches/{branchId}/stock-reservations
POST /admin/branches/{branchId}/stock-releases
```

Tasks:

- Crear paquete `stock.reservation`
- Crear `ReserveStockUseCase`
- Crear `ReleaseStockUseCase`
- Reservar stock al crear pedido si aplica
- Liberar stock al cancelar pedido si aplica
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

---

# Épica 7 — Payments API

**Objetivo:** pagos, payment intents, confirmaciones, cancelaciones, refunds y webhooks.

Path agrupador:

```http
/payments/**
```

Paquete sugerido:

```txt
com.ffresco.payments
```

Futura Lambda:

```txt
ffresco-payments-api
```

## PRs / Tasks

### PR 26 — Payment Intent

```http
POST /payments/orders/{orderId}/payment-intents
GET /payments/orders/{orderId}
GET /payments/orders/{orderId}/status
```

Tasks:

- Crear paquete `payments.intent`
- Crear `CreatePaymentIntentUseCase`
- Crear `GetOrderPaymentUseCase`
- Crear `GetOrderPaymentStatusUseCase`
- Crear ítem `PAYMENT`
- Asociar pago a orden
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 27 — Payment Lifecycle

```http
GET /payments/{paymentId}
POST /payments/{paymentId}/confirm
POST /payments/{paymentId}/cancel
POST /payments/{paymentId}/refund
```

Tasks:

- Crear paquete `payments.lifecycle`
- Crear `GetPaymentUseCase`
- Crear `ConfirmPaymentUseCase`
- Crear `CancelPaymentUseCase`
- Crear `RefundPaymentUseCase`
- Validar transiciones de estado
- Agregar rutas en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

### PR 28 — Mercado Pago Webhook

```http
POST /payments/webhooks/mercado-pago
```

Tasks:

- Crear paquete `payments.webhook`
- Crear `HandleMercadoPagoWebhookUseCase`
- Validar payload
- Actualizar payment status
- Asociar evento externo
- Preparar idempotencia
- Agregar ruta en OpenAPI / API Gateway
- Tests unitarios
- Tests acceptance-style

---

# Épica 8 — Cross-cutting / Platform Quality

**Objetivo:** reglas comunes para que todas las PRs mantengan calidad y sean fáciles de separar mañana.

No es una Lambda futura, pero sí una épica técnica.

## PRs / Tasks

### PR 29 — API Error Standardization

Tasks:

- Definir error response común
- Mapear errores de dominio a HTTP
- Homogeneizar `400`, `404`, `409`, `500`
- Tests de error mapping

### PR 30 — Observability

Tasks:

- Logs estructurados
- Correlation ID
- Request ID
- Métricas básicas por endpoint
- Preparar CloudWatch/New Relic

### PR 31 — Acceptance Test Pattern

Tasks:

- Definir carpeta de tests acceptance-style
- Crear naming convention
- Crear fixture builder para DynamoDB
- Crear helper para ejecutar router/function
- Documentar estrategia

# Épica 7 - Notifications

## Objetivo

Implementar un módulo de notificaciones desacoplado que permita generar, registrar, enviar y reintentar comunicaciones internas o externas originadas por eventos del sistema.

Este módulo debe soportar inicialmente notificaciones internas para cocina y quedar preparado para futuros canales como WhatsApp, Email, SMS, Push y WebSocket.

---

## Contexto arquitectónico

Las notificaciones no deben estar acopladas directamente a los dominios de negocio.

Ejemplo:

- `OrderCreated`
- `PaymentApproved`
- `KitchenTicketCreated`
- `OrderReady`

Estos eventos pueden generar una o más notificaciones.

Regla:

```text
Domain Event -> Notification -> Sender Adapter
```
## Notification Module

### Notification

Representa una comunicación pendiente, enviada o fallida hacia un canal externo o interno.

Campos:
- id
- tenantId
- branchId nullable
- sourceEventId
- sourceEventType
- channel
- recipientType
- recipient
- templateCode
- payload
- status
- attemptCount
- lastError
- scheduledAt
- sentAt
- createdAt
- updatedAt

Canales:
- KITCHEN_SCREEN
- WHATSAPP
- EMAIL
- SMS
- PUSH
- WEBSOCKET
- INTERNAL_EVENT

Estados:
- PENDING
- PROCESSING
- SENT
- FAILED
- CANCELED

-Tener TTL para borrar notificaciones viejas



### NotificationAttempt

Representa cada intento de envío de una notificación.

Campos:
- id
- notificationId
- provider
- status
- requestPayload
- responsePayload
- errorMessage
- createdAt


**Paquetes**

notification
  domain
    Notification
    NotificationChannel
    NotificationStatus
    NotificationAttempt
    NotificationRecipientType

  application
    CreateNotificationUseCase
    SendNotificationUseCase
    RetryFailedNotificationUseCase
    ProcessNotificationEventUseCase

  infrastructure
    DynamoNotificationRepository
    WhatsAppNotificationSender
    WebSocketNotificationSender
    EmailNotificationSender
    SqsNotificationQueuePublisher
    SqsNotificationQueueConsumer

**Criterios de aceptación**
	Se puede crear una notificación a partir de un evento de dominio.
	La notificación queda registrada en DynamoDB con status PENDING.
	La notificación tiene tenantId, branchId, channel, recipient, templateCode y payload.
	La notificación puede ser enviada por un sender adapter.
	Si el envío es exitoso, cambia a SENT y guarda sentAt.
	Si el envío falla, incrementa attemptCount, guarda lastError y queda como FAILED o vuelve a reintento según política.
	Las notificaciones antiguas tienen expiresAt para TTL.
	No se hacen scans globales para procesar notificaciones pendientes.
	El procesamiento asíncrono usa SQS y DLQ.
	El diseño permite agregar WhatsApp, Email, WebSocket o Push sin modificar los dominios de negocio.
---

# Orden recomendado de trabajo

```txt
PR 11 — Bootstrap Demo
PR 1  — Public Menu
PR 2  — Place Order
PR 3  — Customer Orders
PR 4  — Bill
PR 6  — Kitchen Orders List
PR 7  — Update Order Status
PR 5  — Waiter Calls
PR 9  — Operations Waiter Calls
```

Con esos PRs queda el flujo principal:

```txt
cargar demo -> ver menú -> pedir -> ver cuenta -> cocina ve pedido -> cocina cambia estado
```

---

# Prompt ejemplo para pedir una PR a la IA

Este prompt se puede copiar y reutilizar cambiando solo la sección `Feature to implement`.

```md
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

---

## Feature to implement

Implement this PR only:

```http
[HTTP_METHOD] [PATH]
```

Feature name:

```txt
[FEATURE_NAME]
```

Domain package:

```txt
[DOMAIN_PACKAGE]
```

Examples:

```txt
com.ffresco.publicapi.menu
com.ffresco.publicapi.order
com.ffresco.operations.orders
com.ffresco.admin.bootstrap
com.ffresco.catalog.product
com.ffresco.pricing.pricelist
com.ffresco.stock.adjustment
com.ffresco.payments.intent
```

---

## Required implementation shape

Implement this as a vertical slice.

The PR must include, when applicable:

```txt
1. API Gateway/OpenAPI route definition
2. Router mapping
3. Spring Function entrypoint or function handler
4. Request parser / request DTO
5. Application use case
6. Domain model / domain service if needed
7. Input validation
8. Output port interface
9. DynamoDB adapter implementation
10. Response DTO
11. API error mapping
12. Unit tests
13. Acceptance-style endpoint tests
```

---

## Architecture rules

Follow these rules strictly:

1. Router only reads HTTP method/path/query/body and delegates.
2. Router must not contain business logic.
3. Spring Function must be thin.
4. Use case orchestrates the business flow.
5. Business rules must live in use case or domain services, not adapters.
6. DynamoDB access must go through output ports.
7. DynamoDB key construction should be centralized or reusable.
8. DTOs must not leak DynamoDB implementation details.
9. Keep the package cohesive with the domain.
10. Avoid changing unrelated code.

---

## Test strategy

For this PR, add tests according to the existing test strategy.

### Unit tests

Unit tests must cover:
- happy path
- invalid input
- boundary condition
- domain/business rule failure if applicable

### Acceptance-style tests

Acceptance-style tests must:
- exercise the feature from the router/function level
- use realistic request payloads
- validate HTTP status
- validate response body
- validate relevant DynamoDB persistence behavior if applicable

### Minimum test cases

Add at least:

```txt
1. happy path
2. missing or invalid required input
3. not found or conflict scenario
4. one boundary case
```

---

## API Gateway / SAM requirement

If this feature exposes a new endpoint:
- add the route to the OpenAPI/API Gateway definition
- ensure the route points to the existing Lambda integration
- do not break existing routes
- keep path grouping aligned with the domain

---

## DynamoDB rules

Use the existing single-table design.

Generic keys:

```txt
pk
sk
gsi1pk
gsi1sk
entityType
```

Do not create a new DynamoDB table for this PR.

Only create or update item shapes required for this feature.

Use existing table environment variable:

```txt
FFRESCO_CORE_TABLE_NAME
```

---

## Done criteria

The PR is done only when:

```txt
1. Code compiles
2. Tests pass
3. New endpoint is wired in API Gateway/OpenAPI
4. Feature follows router -> function -> use case -> ports -> adapter
5. No business logic is in the router
6. No direct DynamoDB access exists outside adapters
7. Response follows the agreed API contract
8. No unrelated files were modified
9. Package structure makes future Lambda extraction possible
```

---

## Output expected from you

After implementing, summarize:

```txt
1. Files created
2. Files modified
3. Main classes added
4. Tests added
5. Endpoint added to API Gateway/OpenAPI
6. How to run tests
7. How to test manually with curl
```
```

---

# Ejemplo concreto — PR 1 Public Menu

```md
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

This feature belongs to:

```txt
/public/** -> public/customer-api -> com.ffresco.publicapi
```

---

## Feature to implement

Implement this PR only:

```http
GET /public/menu?tableId={tablePublicId}
```

Feature name:

```txt
Get Public Menu
```

Domain package:

```txt
com.ffresco.publicapi.menu
```

---

## Business flow

1. Read `tableId` from query string.
2. Treat it as `tablePublicId`.
3. Validate that `tableId` is present.
4. Resolve table using GSI1:

```txt
gsi1pk = TABLE_PUBLIC#{tablePublicId}
gsi1sk = METADATA
```

5. Validate that table exists.
6. Validate that table is active.
7. Read:
   - tenantId
   - branchId
   - internal tableId
   - tablePublicId
   - tableName
   - tableType
   - activeMenuId
8. Query active public menu:

```txt
pk = PUBLIC_MENU#{tenantId}#{branchId}#{activeMenuId}
```

9. Split items by:
   - MENU_CATEGORY
   - MENU_PRODUCT
10. Return context, categories and products.

---

## Response contract

```json
{
  "context": {
    "tenantId": "t_demo",
    "branchId": "B001",
    "tableId": "tbl_demo_001",
    "tableName": "Mesa 001",
    "tableType": "TABLE",
    "restaurantName": "Shop Test Demo",
    "language": "pt-BR",
    "currency": "BRL",
    "serviceFeeRate": 0.1
  },
  "categories": [
    {
      "id": "cat_bebidas",
      "name": "Bebidas",
      "imageUrl": "https://...",
      "order": 1
    }
  ],
  "products": [
    {
      "id": "prod_coca_zero",
      "name": "Coca Cola Zero",
      "description": "Lata 350ml gelada",
      "price": 8.9,
      "imageUrl": "https://...",
      "categoryId": "cat_bebidas",
      "featured": true,
      "available": true
    }
  ]
}
```

---

## Error cases

Implement consistent API errors for:

```txt
400 missing tableId
404 table not found
409 inactive table
404 active menu not found
```

---

## Required implementation shape

Implement this as a vertical slice.

The PR must include:

```txt
1. API Gateway/OpenAPI route definition
2. Router mapping
3. Spring Function entrypoint or function handler
4. Request parser / request DTO
5. GetPublicMenuUseCase
6. PublicMenuQueryPort
7. DynamoDBPublicMenuAdapter
8. Domain models
9. Response DTO
10. API error mapping
11. Unit tests
12. Acceptance-style endpoint tests
```

---

## Architecture rules

Follow these rules strictly:

1. Router only reads HTTP method/path/query/body and delegates.
2. Router must not contain business logic.
3. Spring Function must be thin.
4. Use case orchestrates the business flow.
5. Business rules must live in use case or domain services, not adapters.
6. DynamoDB access must go through output ports.
7. DynamoDB key construction should be centralized or reusable.
8. DTOs must not leak DynamoDB implementation details.
9. Keep the package cohesive with the domain.
10. Avoid changing unrelated code.

---

## Test strategy

### Unit tests

Add unit tests for:

```txt
happy path
missing tableId
table not found
inactive table
active menu not found
menu with categories and products
menu with categories but no products
```

### Acceptance-style tests

Add at least one acceptance-style test that exercises the endpoint from the router/function level.

It must validate:

```txt
HTTP 200
response context
categories
products
```

Add acceptance-style tests for:

```txt
400 missing tableId
404 table not found
409 inactive table
```

---

## API Gateway / SAM requirement

Add the route to the OpenAPI/API Gateway definition:

```http
GET /public/menu
```

Ensure the route points to the existing Lambda integration.

Do not break existing routes.

---

## DynamoDB rules

Use the existing single-table design.

Generic keys:

```txt
pk
sk
gsi1pk
gsi1sk
entityType
```

Do not create a new DynamoDB table for this PR.

Use existing table environment variable:

```txt
FFRESCO_CORE_TABLE_NAME
```

---

## Done criteria

The PR is done only when:

```txt
1. Code compiles
2. Tests pass
3. New endpoint is wired in API Gateway/OpenAPI
4. Feature follows router -> function -> use case -> ports -> adapter
5. No business logic is in the router
6. No direct DynamoDB access exists outside adapters
7. Response follows the agreed API contract
8. No unrelated files were modified
9. Package structure makes future Lambda extraction possible
```

---

## Output expected from you

After implementing, summarize:

```txt
1. Files created
2. Files modified
3. Main classes added
4. Tests added
5. Endpoint added to API Gateway/OpenAPI
6. How to run tests
7. How to test manually with curl
```
```
