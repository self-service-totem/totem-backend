# Data Model Standard

## Objetivo

Este documento es el source of truth del modelo de datos para Totem SaaS.

Cumple dos funciones:

```text
1. Modelo conceptual para entender entidades, ownership y relaciones.
2. Modelo DynamoDB para implementar access patterns sin scans.
```

## Decisión principal MVP

```text
1 tabla DynamoDB principal
1 API/Lambda inicial
Separación lógica por business capability
Single-table design
Multi-tenant by key design
```

Tabla principal:

```text
ffresco-core-${Environment}
```

Keys:

```text
pk
sk
```

Índices iniciales:

```text
GSI1_PUBLIC_LOOKUP
GSI2_BRANCH_WORK_QUEUE
```

Futuro probable:

```text
ffresco-outbox-${Environment}
```

## Principios de modelado

### Tenant space first

Todo dato operativo de un cliente debe incluir `tenantId` en la clave principal o en la clave de índice.

```text
TENANT#<tenantId>
TENANT#<tenantId>#BRANCH#<branchId>
TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
```

### Business capability first

El modelo acompaña la separación del código por capability.

Ejemplos:

```text
publicapi.menu
restaurant
catalog
ordering
payment
kitchen
notification
common
```

### No scans

No diseñar flujos críticos con `Scan`.

Cada endpoint debe resolverse con:

```text
GetItem
Query
BatchGetItem
TransactWriteItems
```

Si un access pattern no entra en PK/SK, se define un GSI o read model materializado.

### Read models para endpoints públicos

Los endpoints públicos deben ser rápidos y baratos.

Ejemplo para menú público:

```text
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = MENU#PUBLIC
```

Ese item puede contener categorías y productos visibles para evitar múltiples queries en el MVP.

### Frontend nunca manda precios confiables

El frontend puede mostrar precios, pero el backend recalcula:

```text
unitPrice
subtotal
discounts
taxes
serviceFee
total
```

El pedido siempre se valida contra el menú/precio activo del backend.

## Atributos técnicos comunes

Todo item debería tener:

```text
pk
sk
entityType
tenantId
branchId nullable
createdAt
updatedAt
version optional
status optional
```

## Entidades conceptuales principales

### SaaS / tenant

```text
Tenant
Plan
Subscription
User
Membership
Branch
Device
```

Reglas:

```text
- User no pertenece directamente a Tenant; se asocia por Membership.
- Subscription es entidad propia, no un campo embebido completo dentro de Tenant.
- Device representa kiosk, kitchen screen, cashier terminal o tablet.
```

### Catálogo / menú

```text
Category
Product
ProductVariant
ModifierGroup
Modifier
PriceList
PriceListItem
PublicMenu
CatalogVersion
```

Reglas:

```text
- Product es global al tenant.
- Precio y disponibilidad pueden variar por branch.
- OrderItem guarda snapshot de nombre y precio.
- PublicMenu puede ser un read model materializado.
```

### Restaurante / operación

```text
RestaurantTable
TableSession
Order
OrderItem
KitchenOrder
KitchenOrderItem
WaiterCall
```

Reglas:

```text
- RestaurantTable representa la mesa física.
- TableSession representa una sesión abierta de atención.
- Order tiene cabecera y detalle separado.
- KitchenOrder puede derivarse de Order para operación de cocina.
```

### Pagos

```text
PaymentIntent
Payment
Refund
```

Reglas:

```text
- El backend propio no procesa tarjeta directamente.
- Se integra con proveedor externo.
- PaymentIntent representa intención/flujo externo.
- Payment representa resultado confirmado.
```

### Stock

```text
StockBalance
StockMovement
StockReservation
```

Reglas:

```text
- StockBalance representa saldo actual.
- StockMovement representa historial.
- Reservation evita vender stock no disponible cuando el flujo lo requiera.
```

### Notificaciones

```text
Notification
NotificationAttempt
```

Canales sugeridos:

```text
KITCHEN_SCREEN
WHATSAPP
EMAIL
SMS
PUSH
WEBSOCKET
INTERNAL_EVENT
```

Estados sugeridos:

```text
PENDING
PROCESSING
SENT
FAILED
CANCELED
```

Regla:

```text
Usar TTL para notificaciones viejas cuando aplique.
```

### Eventos / outbox

```text
OutboxEvent
```

Regla:

```text
Los eventos importantes se publican mediante outbox si se necesita consistencia event-driven.
```

## DynamoDB key patterns iniciales

### Tenant

```text
PK = TENANT#<tenantId>
SK = METADATA
```

### Branch

```text
PK = TENANT#<tenantId>
SK = BRANCH#<branchId>
```

### Public table lookup

```text
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = TABLE#<tableId>
GSI1PK = PUBLIC_TABLE#<tablePublicId>
GSI1SK = TENANT#<tenantId>#BRANCH#<branchId>#TABLE#<tableId>
```

### Public menu read model

```text
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = MENU#PUBLIC
```

### Order by table session

```text
PK = TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
SK = ORDER#<orderId>
```

### Kitchen work queue

```text
GSI2PK = TENANT#<tenantId>#BRANCH#<branchId>#KITCHEN
GSI2SK = STATUS#<status>#CREATED_AT#<createdAt>#ORDER#<orderId>
```

## Regla para tickets que tocan datos

Cada ticket que cree o modifique persistencia debe declarar:

```text
1. Entidades afectadas.
2. Access patterns necesarios.
3. PK/SK/GSI usados.
4. Ejemplos de items mínimos.
5. Si requiere read model o no.
6. Si requiere transacción o no.
7. Si queda fuera del MVP.
```

El ticket no debe copiar todo este documento. Solo debe describir el caso concreto afectado.
