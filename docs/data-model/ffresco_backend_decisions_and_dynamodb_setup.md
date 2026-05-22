# FFresco / Totem SaaS - DynamoDB Data Model

## 1. Objetivo

Este documento define el modelo de datos DynamoDB para el backend de FFresco / Totem SaaS.

La intención es que funcione como fuente de verdad para:

- Implementación con Claude Code / Cursor.
- Diseño de adapters DynamoDB.
- Evolución futura desde una Lambda modular hacia varias Lambdas por capability.
- Discusión de access patterns antes de crear nuevas entidades.
- Mantener consistencia multi-tenant.

Este documento reemplaza y evoluciona el diseño inicial de `ffresco_backend_decisions_and_dynamodb_setup.md`.

---

## 2. Decisión principal

Para el MVP se usará:

```txt
1 tabla DynamoDB principal
1 API/Lambda inicial
Separación lógica por business capability
Single table design
Multi-tenant by key design
```

Tabla principal:

```txt
ffresco-core-${Environment}
```

Keys principales:

```txt
pk
sk
```

Índices iniciales recomendados:

```txt
GSI1_PUBLIC_LOOKUP
GSI2_BRANCH_WORK_QUEUE
```

Índices futuros posibles:

```txt
GSI3_BRANCH_SESSIONS
GSI4_ENTITY_LOOKUP
```

Tabla futura recomendada para eventos/outbox:

```txt
ffresco-outbox-${Environment}
```

---

## 3. Principios de modelado

### 3.1 Tenant space first

La mayoría de los datos operativos deben vivir dentro del espacio del tenant.

Formato base:

```txt
TENANT#<tenantId>
TENANT#<tenantId>#BRANCH#<branchId>
TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
```

Regla:

```txt
Todo lo que pertenezca a un tenant debe incluir tenantId en la clave principal o en la clave del índice.
```

Esto evita mezclar datos entre tenants y facilita futuras policies, auditoría, migraciones y separación por cliente.

---

### 3.2 Business capability first

Los paquetes de código se organizan por capability:

```txt
com.ffresco.totem.publicapi.menu
com.ffresco.totem.restaurant
com.ffresco.totem.catalog
com.ffresco.totem.ordering
com.ffresco.totem.payment
com.ffresco.totem.kitchen
com.ffresco.totem.common
```

El modelo DynamoDB debe acompañar esta idea usando prefijos claros en `sk` y `entityType`.

Ejemplos:

```txt
TENANT
BRANCH
TABLE
PUBLIC_TABLE
PUBLIC_MENU
TABLE_SESSION
ORDER
ORDER_ITEM
WAITER_CALL
PAYMENT_INTENT
OUTBOX_EVENT
```

---

### 3.3 No scans

No se debe diseñar ningún flujo crítico que requiera `Scan`.

Regla:

```txt
Cada endpoint debe resolverse con GetItem, Query, BatchGetItem o TransactWriteItems.
```

Si un access pattern requiere buscar por un dato que no está en la PK/SK principal, se debe crear un GSI o una proyección materializada.

---

### 3.4 Read models para endpoints públicos

Los endpoints públicos deben ser rápidos y baratos.

Para el MVP, el menú público se modela como un único item materializado:

```txt
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = MENU#PUBLIC
```

Ese item contiene categorías y productos visibles.

Motivo:

- El endpoint `GET /public/menu` es read-only.
- Se espera alto volumen de lectura.
- Evita múltiples queries.
- Simplifica el MVP.
- Permite migrar luego a items por categoría/producto si el menú crece demasiado.

---

### 3.5 Frontend nunca manda precios confiables

El frontend puede mostrar precios, pero el backend siempre recalcula:

```txt
subtotal
serviceFee
total
unitPrice
discounts
taxes
```

El pedido debe validarse contra el menú activo del backend.

---

## 4. Tabla principal: ffresco-core-${Environment}

### 4.1 Key schema

```txt
TableName = ffresco-core-${Environment}

pk = string
sk = string
```

### 4.2 Atributos técnicos comunes

Todo item debería tener, cuando aplique:

```txt
entityType
tenantId
branchId
createdAt
updatedAt
createdBy
updatedBy
version
status
```

Ejemplo:

```json
{
  "entityType": "ORDER",
  "tenantId": "t001",
  "branchId": "b001",
  "createdAt": "2026-05-22T15:30:00Z",
  "updatedAt": "2026-05-22T15:30:00Z",
  "version": 1,
  "status": "PENDING"
}
```

### 4.3 Reglas sobre dinero

No guardar dinero como `double`.

Preferido:

```json
{
  "amount": "5.00",
  "currency": "BRL"
}
```

Alternativa técnica futura:

```json
{
  "amountMinor": 500,
  "currency": "BRL"
}
```

Para el dominio Java, usar `BigDecimal` o un `Money` value object.

---

## 5. Índices de la tabla principal

## 5.1 GSI1_PUBLIC_LOOKUP

Uso principal:

```txt
Resolver IDs públicos que vienen desde QR, kiosk o links públicos.
```

Key schema:

```txt
gsi1pk
gsi1sk
```

Nombre recomendado:

```txt
GSI1_PUBLIC_LOOKUP
```

Ejemplo para mesa pública:

```txt
gsi1pk = PUBLIC_TABLE#tbl_pub_8H7K2X
gsi1sk = TENANT#t001#BRANCH#b001#TABLE#table001
```

Access pattern:

```txt
GET /public/menu?tableId=tbl_pub_8H7K2X
```

Query:

```txt
IndexName = GSI1_PUBLIC_LOOKUP
gsi1pk = PUBLIC_TABLE#tbl_pub_8H7K2X
```

Resultado:

```txt
tenantId
branchId
tableId
activeMenuId
tableName
```

Nota:

```txt
Si más adelante el tenant viene en host, subdominio o path, se puede resolver la mesa con GetItem tenant-scoped.
Mientras el endpoint solo reciba tablePublicId, GSI1 evita hacer Scan.
```

---

## 5.2 GSI2_BRANCH_WORK_QUEUE

Uso principal:

```txt
Pantallas operativas por sucursal.
Cocina.
Mozo.
Admin operativo.
Pedidos pendientes.
Llamados abiertos.
Pagos pendientes.
```

Key schema:

```txt
gsi2pk
gsi2sk
```

Nombre recomendado:

```txt
GSI2_BRANCH_WORK_QUEUE
```

Formato:

```txt
gsi2pk = TENANT#<tenantId>#BRANCH#<branchId>#WORK#<workType>#STATUS#<status>
gsi2sk = <createdAt>#<entityId>
```

Ejemplos:

```txt
TENANT#t001#BRANCH#b001#WORK#ORDER#STATUS#PENDING
TENANT#t001#BRANCH#b001#WORK#ORDER#STATUS#PREPARING
TENANT#t001#BRANCH#b001#WORK#WAITER_CALL#STATUS#OPEN
TENANT#t001#BRANCH#b001#WORK#PAYMENT#STATUS#PENDING
```

Access patterns:

```txt
Kitchen screen: listar pedidos PENDING/PREPARING.
Waiter screen: listar waiter calls OPEN.
Admin screen: listar pagos PENDING.
```

---

## 5.3 GSI3_BRANCH_SESSIONS - futuro opcional

No crear de entrada salvo que aparezca el access pattern.

Uso:

```txt
Listar sesiones abiertas de una sucursal.
Dashboard del salón.
Admin de mesas.
```

Key schema:

```txt
gsi3pk
gsi3sk
```

Formato:

```txt
gsi3pk = TENANT#<tenantId>#BRANCH#<branchId>#TABLE_SESSION_STATUS#<status>
gsi3sk = <openedAt>#<tableSessionId>
```

Ejemplo:

```txt
gsi3pk = TENANT#t001#BRANCH#b001#TABLE_SESSION_STATUS#OPEN
gsi3sk = 2026-05-22T15:30:00Z#ts001
```

---

## 5.4 GSI4_ENTITY_LOOKUP - futuro opcional

No crear de entrada salvo necesidad real.

Uso:

```txt
Buscar entidades por ID global interno sin conocer su PK natural.
Debug.
Admin.
Integraciones.
```

Key schema:

```txt
gsi4pk
gsi4sk
```

Formato:

```txt
gsi4pk = ENTITY#<entityType>#<entityId>
gsi4sk = TENANT#<tenantId>#BRANCH#<branchId>
```

Ejemplo:

```txt
gsi4pk = ENTITY#ORDER#ord001
gsi4sk = TENANT#t001#BRANCH#b001
```

---

## 6. Dominios y entidades

## 6.1 Restaurant / Business Context

Responsable de:

```txt
Tenant
Restaurant
Branch
Tables
QR codes
Kiosks
Business settings
Currency
Language
Service fee
Opening hours
```

---

### 6.1.1 Tenant / Restaurant metadata

```txt
pk = TENANT#<tenantId>
sk = METADATA
entityType = TENANT
```

Ejemplo:

```json
{
  "entityType": "TENANT",
  "tenantId": "t001",
  "restaurantName": "Pertinho do Ceu",
  "logoUrl": "https://example.com/logo.png",
  "language": "pt-BR",
  "currency": "BRL",
  "serviceFeeRate": "0.10",
  "status": "ACTIVE"
}
```

Access patterns:

```txt
Get tenant metadata by tenantId.
```

---

### 6.1.2 Branch / Sucursal

```txt
pk = TENANT#<tenantId>
sk = BRANCH#<branchId>
entityType = BRANCH
```

Ejemplo:

```json
{
  "entityType": "BRANCH",
  "tenantId": "t001",
  "branchId": "b001",
  "name": "Sucursal Centro",
  "address": "Rua Centro 123",
  "status": "ACTIVE",
  "timezone": "America/Sao_Paulo"
}
```

Access patterns:

```txt
Listar branches de un tenant:
PK = TENANT#t001
SK begins_with BRANCH#
```

---

### 6.1.3 Table / Mesa física

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>
sk = TABLE#<tableId>
entityType = TABLE
gsi1pk = PUBLIC_TABLE#<tablePublicId>
gsi1sk = TENANT#<tenantId>#BRANCH#<branchId>#TABLE#<tableId>
```

Ejemplo:

```json
{
  "entityType": "TABLE",
  "tenantId": "t001",
  "branchId": "b001",
  "tableId": "table001",
  "tablePublicId": "tbl_pub_8H7K2X",
  "tableName": "Mesa 140",
  "active": true,
  "activeMenuId": "menu001",
  "currentTableSessionId": "ts001"
}
```

Access patterns:

```txt
Get table by tenant/branch/tableId.
Resolve public table from QR using GSI1.
List branch tables.
```

Notas:

```txt
tablePublicId debe ser difícil de adivinar.
No usar mesa-01 como ID público.
```

---

### 6.1.4 Kiosk / Tótem

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>
sk = KIOSK#<kioskId>
entityType = KIOSK
gsi1pk = KIOSK_PUBLIC#<kioskPublicId>
gsi1sk = TENANT#<tenantId>#BRANCH#<branchId>#KIOSK#<kioskId>
```

Ejemplo:

```json
{
  "entityType": "KIOSK",
  "tenantId": "t001",
  "branchId": "b001",
  "kioskId": "kiosk001",
  "kioskPublicId": "kiosk_pub_3H2X9Q",
  "name": "Totem Entrada",
  "status": "ACTIVE",
  "activeMenuId": "menu001"
}
```

---

## 6.2 Catalog / Menu Context

Responsable de:

```txt
Categories
Products
Price lists
Public menu projection
Availability
Featured products
Promotions
Basic stock
```

---

### 6.2.1 Product master

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>
sk = PRODUCT#<productId>
entityType = PRODUCT
```

Ejemplo:

```json
{
  "entityType": "PRODUCT",
  "tenantId": "t001",
  "branchId": "b001",
  "productId": "prod-coca-zero",
  "name": "Coca Cola Zero",
  "description": "Lata 350ml gelada",
  "basePrice": {
    "amount": "8.90",
    "currency": "BRL"
  },
  "imageUrl": "https://example.com/coca.png",
  "status": "ACTIVE"
}
```

Access patterns:

```txt
Get product by branch/productId.
List products by branch.
```

---

### 6.2.2 Category master

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>
sk = CATEGORY#<categoryId>
entityType = CATEGORY
```

Ejemplo:

```json
{
  "entityType": "CATEGORY",
  "tenantId": "t001",
  "branchId": "b001",
  "categoryId": "cat-bebidas",
  "name": "Bebidas",
  "displayOrder": 1,
  "status": "ACTIVE"
}
```

---

### 6.2.3 Price list / Menu version metadata

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>
sk = MENU_VERSION#<menuId>
entityType = MENU_VERSION
```

Ejemplo:

```json
{
  "entityType": "MENU_VERSION",
  "tenantId": "t001",
  "branchId": "b001",
  "menuId": "menu001",
  "name": "Menu Principal",
  "status": "ACTIVE",
  "publishedAt": "2026-05-22T10:00:00Z"
}
```

---

### 6.2.4 Public menu materialized item - MVP

Decisión MVP:

```txt
Un solo item materializado para el menú público.
```

Key:

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>
sk = MENU#PUBLIC
entityType = PUBLIC_MENU
```

Ejemplo:

```json
{
  "entityType": "PUBLIC_MENU",
  "tenantId": "t001",
  "branchId": "b001",
  "menuId": "menu001",
  "currency": "BRL",
  "publishedAt": "2026-05-22T10:00:00Z",
  "categories": [
    {
      "id": "cat-bebidas",
      "name": "Bebidas",
      "displayOrder": 1,
      "products": [
        {
          "id": "prod-coca-zero",
          "name": "Coca Cola Zero",
          "description": "Lata 350ml gelada",
          "price": {
            "amount": "8.90",
            "currency": "BRL"
          },
          "imageUrl": "https://example.com/coca.png",
          "featured": true,
          "available": true
        }
      ]
    }
  ]
}
```

Access patterns:

```txt
Get public menu for branch.
Get product detail by reading the public menu and finding the product.
```

Regla de productos unavailable:

```txt
Por defecto, no devolver productos con available=false en el menú público.
El menú customer-facing solo muestra productos que se pueden pedir.
Si luego negocio quiere mostrar productos agotados, agregar includeUnavailable=true o una configuración.
```

---

### 6.2.5 Public menu split items - evolución futura

Cuando el menú crezca o haya necesidad de updates parciales, se puede migrar a:

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>#MENU#PUBLIC
sk = CATEGORY#<displayOrder>#<categoryId>

pk = TENANT#<tenantId>#BRANCH#<branchId>#MENU#PUBLIC
sk = PRODUCT#<categoryId>#<displayOrder>#<productId>
```

No usar en MVP salvo que el item materializado se vuelva demasiado grande o difícil de actualizar.

---

## 6.3 Ordering Context

Responsable de:

```txt
Table sessions
Orders
Order items
Kitchen status
Bill
Close bill requests
Waiter calls
```

---

### 6.3.1 TableSession

Una mesa física no es una cuenta abierta.

La cuenta abierta se modela como:

```txt
TableSession
```

Key:

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
sk = METADATA
entityType = TABLE_SESSION
```

Ejemplo:

```json
{
  "entityType": "TABLE_SESSION",
  "tenantId": "t001",
  "branchId": "b001",
  "tableId": "table001",
  "tablePublicId": "tbl_pub_8H7K2X",
  "tableSessionId": "ts001",
  "status": "OPEN",
  "openedAt": "2026-05-22T12:00:00Z",
  "closedAt": null,
  "subtotal": {
    "amount": "0.00",
    "currency": "BRL"
  },
  "serviceFee": {
    "amount": "0.00",
    "currency": "BRL"
  },
  "total": {
    "amount": "0.00",
    "currency": "BRL"
  }
}
```

Access patterns:

```txt
Get session by tableSessionId.
Get all orders/calls/payments for a session.
```

---

### 6.3.2 Active session pointer

Para encontrar rápido la sesión abierta de una mesa:

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>
sk = TABLE#<tableId>#ACTIVE_SESSION
entityType = ACTIVE_TABLE_SESSION
```

Ejemplo:

```json
{
  "entityType": "ACTIVE_TABLE_SESSION",
  "tenantId": "t001",
  "branchId": "b001",
  "tableId": "table001",
  "tableSessionId": "ts001",
  "status": "OPEN",
  "openedAt": "2026-05-22T12:00:00Z"
}
```

Uso:

```txt
POST /public/tables/{tablePublicId}/orders
1. Resuelve tablePublicId.
2. GetItem ACTIVE_SESSION.
3. Si no existe, crea TableSession + ActiveSession pointer en una transacción.
```

---

### 6.3.3 Order

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
sk = ORDER#<createdAt>#<orderId>
entityType = ORDER

gsi2pk = TENANT#<tenantId>#BRANCH#<branchId>#WORK#ORDER#STATUS#<status>
gsi2sk = <createdAt>#<orderId>
```

Ejemplo:

```json
{
  "entityType": "ORDER",
  "tenantId": "t001",
  "branchId": "b001",
  "tableSessionId": "ts001",
  "tableId": "table001",
  "tablePublicId": "tbl_pub_8H7K2X",
  "orderId": "ord001",
  "orderNumber": "1042",
  "customerName": "Fernando",
  "status": "PENDING",
  "createdAt": "2026-05-22T12:34:56Z",
  "subtotal": {
    "amount": "17.80",
    "currency": "BRL"
  },
  "serviceFee": {
    "amount": "1.78",
    "currency": "BRL"
  },
  "total": {
    "amount": "19.58",
    "currency": "BRL"
  },
  "items": [
    {
      "productId": "prod-coca-zero",
      "name": "Coca Cola Zero",
      "quantity": 2,
      "unitPrice": {
        "amount": "8.90",
        "currency": "BRL"
      },
      "notes": ""
    }
  ],
  "gsi2pk": "TENANT#t001#BRANCH#b001#WORK#ORDER#STATUS#PENDING",
  "gsi2sk": "2026-05-22T12:34:56Z#ord001"
}
```

Status iniciales:

```txt
PENDING
CONFIRMED
PREPARING
READY
DELIVERED
CANCELLED
```

Access patterns:

```txt
List orders by table session.
List pending/preparing orders for kitchen.
Get order detail within session.
```

---

### 6.3.4 Waiter Call

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
sk = WAITER_CALL#<createdAt>#<waiterCallId>
entityType = WAITER_CALL

gsi2pk = TENANT#<tenantId>#BRANCH#<branchId>#WORK#WAITER_CALL#STATUS#<status>
gsi2sk = <createdAt>#<waiterCallId>
```

Ejemplo:

```json
{
  "entityType": "WAITER_CALL",
  "tenantId": "t001",
  "branchId": "b001",
  "tableSessionId": "ts001",
  "waiterCallId": "wc001",
  "customerName": "Fernando",
  "phone": "+5581999991234",
  "reason": "CALL_WAITER",
  "status": "OPEN",
  "createdAt": "2026-05-22T12:40:00Z",
  "gsi2pk": "TENANT#t001#BRANCH#b001#WORK#WAITER_CALL#STATUS#OPEN",
  "gsi2sk": "2026-05-22T12:40:00Z#wc001"
}
```

Reasons:

```txt
CALL_WAITER
REQUEST_BILL
ASK_ORDER_STATUS
OTHER
```

Status:

```txt
OPEN
ACKNOWLEDGED
RESOLVED
CANCELLED
```

---

### 6.3.5 Bill / Account projection

Para MVP, la cuenta se puede calcular leyendo los orders de la session.

Cuando crezca, crear proyección:

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
sk = BILL#CURRENT
entityType = BILL
```

Ejemplo:

```json
{
  "entityType": "BILL",
  "tenantId": "t001",
  "branchId": "b001",
  "tableSessionId": "ts001",
  "status": "OPEN",
  "subtotal": {
    "amount": "100.00",
    "currency": "BRL"
  },
  "serviceFee": {
    "amount": "10.00",
    "currency": "BRL"
  },
  "total": {
    "amount": "110.00",
    "currency": "BRL"
  },
  "paidAmount": {
    "amount": "30.00",
    "currency": "BRL"
  },
  "remainingAmount": {
    "amount": "80.00",
    "currency": "BRL"
  }
}
```

---

## 6.4 Payment Context

Responsable futuro de:

```txt
Payment intent
Cash payment
Card payment
Mercado Pago
Refunds
Payment status
Receipts
```

---

### 6.4.1 Payment Intent

```txt
pk = TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
sk = PAYMENT_INTENT#<createdAt>#<paymentIntentId>
entityType = PAYMENT_INTENT

gsi2pk = TENANT#<tenantId>#BRANCH#<branchId>#WORK#PAYMENT#STATUS#<status>
gsi2sk = <createdAt>#<paymentIntentId>
```

Ejemplo:

```json
{
  "entityType": "PAYMENT_INTENT",
  "tenantId": "t001",
  "branchId": "b001",
  "tableSessionId": "ts001",
  "paymentIntentId": "pay001",
  "provider": "MERCADO_PAGO",
  "status": "PENDING",
  "amount": {
    "amount": "50.00",
    "currency": "BRL"
  },
  "createdAt": "2026-05-22T13:00:00Z",
  "gsi2pk": "TENANT#t001#BRANCH#b001#WORK#PAYMENT#STATUS#PENDING",
  "gsi2sk": "2026-05-22T13:00:00Z#pay001"
}
```

Status:

```txt
PENDING
AUTHORIZED
PAID
FAILED
CANCELLED
REFUNDED
```

---

## 7. Access patterns principales

## 7.1 GET /public/menu?tableId={tablePublicId}

Flujo:

```txt
1. Query GSI1_PUBLIC_LOOKUP:
   gsi1pk = PUBLIC_TABLE#<tablePublicId>

2. Obtener:
   tenantId
   branchId
   tableId
   activeMenuId
   tableName

3. GetItem menú materializado:
   pk = TENANT#<tenantId>#BRANCH#<branchId>
   sk = MENU#PUBLIC

4. Filtrar products.available = true.

5. Devolver JSON:API response.
```

---

## 7.2 GET /public/menu/products/{productId}?tableId={tablePublicId}

Flujo MVP:

```txt
1. Resolver tablePublicId por GSI1.
2. GetItem MENU#PUBLIC.
3. Buscar productId dentro del menú materializado.
4. Si existe y available=true, devolver detalle.
5. Si no existe o no pertenece al menú de esa branch, retornar 404 business error.
```

Nota:

```txt
Aunque leer el menú entero para un detalle parezca menos óptimo, para MVP simplifica.
Si el menú crece, migrar a split items por producto.
```

---

## 7.3 POST /public/tables/{tablePublicId}/orders

Flujo:

```txt
1. Resolver tablePublicId por GSI1.
2. Resolver o crear TableSession OPEN.
3. Leer MENU#PUBLIC.
4. Validar productos y cantidades.
5. Filtrar/validar que productos estén available=true.
6. Calcular precios en backend.
7. Crear ORDER dentro de la session.
8. Actualizar ACTIVE_SESSION / BILL si aplica.
9. Escribir OUTBOX_EVENT si hay integración/eventos.
10. Devolver orden creada.
```

Transacción recomendada:

```txt
TransactWriteItems:
- Put ORDER
- Update/Put BILL#CURRENT
- Put OUTBOX_EVENT, si se usa outbox
```

---

## 7.4 Kitchen screen

Query:

```txt
IndexName = GSI2_BRANCH_WORK_QUEUE
gsi2pk = TENANT#<tenantId>#BRANCH#<branchId>#WORK#ORDER#STATUS#PENDING
```

También:

```txt
STATUS#PREPARING
STATUS#READY
```

---

## 7.5 Waiter screen

Query:

```txt
IndexName = GSI2_BRANCH_WORK_QUEUE
gsi2pk = TENANT#<tenantId>#BRANCH#<branchId>#WORK#WAITER_CALL#STATUS#OPEN
```

---

## 7.6 Admin branch sessions - futuro

Si se necesita listar sesiones abiertas:

```txt
IndexName = GSI3_BRANCH_SESSIONS
gsi3pk = TENANT#<tenantId>#BRANCH#<branchId>#TABLE_SESSION_STATUS#OPEN
```

Si no existe GSI3, mantener este endpoint fuera del MVP o resolverlo con otra proyección.

---

## 8. Outbox / eventos asíncronos

## 8.1 Decisión recomendada

Para el MVP simple, se puede no implementar outbox todavía.

Cuando se agreguen colas, integraciones o eventos reales, se recomienda una tabla separada:

```txt
ffresco-outbox-${Environment}
```

Motivo:

- Evita mezclar polling de eventos con la tabla core.
- Permite TTL propio.
- Permite retries y locking sin ensuciar entidades de negocio.
- Permite escalar consumidores/event processors por separado.
- Mantiene más claro el modelo operativo.

---

## 8.2 Tabla outbox recomendada

```txt
TableName = ffresco-outbox-${Environment}

pk
sk

GSI1_OUTBOX_BY_AGGREGATE:
gsi1pk
gsi1sk
```

Key principal para eventos pendientes:

```txt
pk = OUTBOX#STATUS#<status>#BUCKET#<yyyyMMdd>#<bucketNumber>
sk = <createdAt>#<eventId>
```

Ejemplo:

```txt
pk = OUTBOX#STATUS#PENDING#BUCKET#20260522#03
sk = 2026-05-22T12:34:56Z#evt001
```

Item:

```json
{
  "eventId": "evt001",
  "eventType": "ORDER_CREATED",
  "tenantId": "t001",
  "branchId": "b001",
  "aggregateType": "ORDER",
  "aggregateId": "ord001",
  "status": "PENDING",
  "createdAt": "2026-05-22T12:34:56Z",
  "availableAt": "2026-05-22T12:34:56Z",
  "attempts": 0,
  "maxAttempts": 5,
  "payload": {
    "orderId": "ord001",
    "tableSessionId": "ts001"
  }
}
```

Status:

```txt
PENDING
PROCESSING
PROCESSED
FAILED
DEAD_LETTER
```

---

## 8.3 GSI1_OUTBOX_BY_AGGREGATE

Uso:

```txt
Auditar eventos generados por una entidad.
Debug.
Reprocesamiento manual.
```

Formato:

```txt
gsi1pk = AGGREGATE#<aggregateType>#<aggregateId>
gsi1sk = <createdAt>#<eventId>
```

Ejemplo:

```txt
gsi1pk = AGGREGATE#ORDER#ord001
gsi1sk = 2026-05-22T12:34:56Z#evt001
```

---

## 8.4 Outbox dentro de core table - alternativa MVP

Si se quiere evitar crear otra tabla al principio:

```txt
pk = TENANT#<tenantId>#OUTBOX#STATUS#<status>#BUCKET#<yyyyMMdd>#<bucketNumber>
sk = <createdAt>#<eventId>
entityType = OUTBOX_EVENT
```

Pero esta opción debe ser temporal.

Recomendación:

```txt
Core table para datos de negocio.
Outbox table para eventos operativos.
```

---

## 9. Convenciones de nombres

## 9.1 IDs públicos

```txt
tablePublicId = tbl_pub_<random>
kioskPublicId = kiosk_pub_<random>
menuPublicId = menu_pub_<random> // si se necesita
```

Reglas:

```txt
No usar nombres secuenciales públicos.
No usar mesa-01 como ID público.
No exponer tenantId/branchId si no es necesario.
```

---

## 9.2 IDs internos

```txt
tenantId = t001
branchId = b001
tableId = table001
tableSessionId = ts001
orderId = ord001
paymentIntentId = pay001
waiterCallId = wc001
```

Para producción, usar IDs no predecibles o UUID/ULID.

---

## 9.3 Timestamps

Usar ISO-8601 UTC:

```txt
2026-05-22T12:34:56Z
```

Los timestamps en `sk` deben ordenar cronológicamente.

---

## 10. JSON:API responses

Los endpoints públicos deben devolver JSON:API.

Ejemplo `GET /public/menu`:

```json
{
  "data": {
    "type": "public-menu",
    "id": "branch-001-menu",
    "attributes": {
      "branchId": "b001",
      "tableId": "tbl_pub_8H7K2X",
      "currency": "BRL",
      "categories": [
        {
          "id": "cat-bebidas",
          "name": "Bebidas",
          "products": [
            {
              "id": "prod-coca-zero",
              "name": "Coca Cola Zero",
              "description": "Lata 350ml gelada",
              "price": {
                "amount": "8.90",
                "currency": "BRL"
              },
              "available": true
            }
          ]
        }
      ]
    }
  }
}
```

Ejemplo error:

```json
{
  "errors": [
    {
      "status": "404",
      "code": "PUBLIC_TABLE_NOT_FOUND",
      "title": "Public table not found",
      "detail": "No public table was found for tableId tbl_pub_invalid."
    }
  ]
}
```

---

## 11. CloudFormation / SAM - Core table

```yaml
FfrescoCoreTable:
  Type: AWS::DynamoDB::Table
  Properties:
    TableName: !Sub "ffresco-core-${Environment}"
    BillingMode: PAY_PER_REQUEST
    AttributeDefinitions:
      - AttributeName: pk
        AttributeType: S
      - AttributeName: sk
        AttributeType: S
      - AttributeName: gsi1pk
        AttributeType: S
      - AttributeName: gsi1sk
        AttributeType: S
      - AttributeName: gsi2pk
        AttributeType: S
      - AttributeName: gsi2sk
        AttributeType: S
    KeySchema:
      - AttributeName: pk
        KeyType: HASH
      - AttributeName: sk
        KeyType: RANGE
    GlobalSecondaryIndexes:
      - IndexName: GSI1_PUBLIC_LOOKUP
        KeySchema:
          - AttributeName: gsi1pk
            KeyType: HASH
          - AttributeName: gsi1sk
            KeyType: RANGE
        Projection:
          ProjectionType: ALL
      - IndexName: GSI2_BRANCH_WORK_QUEUE
        KeySchema:
          - AttributeName: gsi2pk
            KeyType: HASH
          - AttributeName: gsi2sk
            KeyType: RANGE
        Projection:
          ProjectionType: ALL
    PointInTimeRecoverySpecification:
      PointInTimeRecoveryEnabled: true
    SSESpecification:
      SSEEnabled: true
    Tags:
      - Key: Project
        Value: ffresco
      - Key: Environment
        Value: !Ref Environment
```

---

## 12. CloudFormation / SAM - Outbox table futura

No crear necesariamente en el PR actual.

Crear cuando se implementen eventos asíncronos reales.

```yaml
FfrescoOutboxTable:
  Type: AWS::DynamoDB::Table
  Properties:
    TableName: !Sub "ffresco-outbox-${Environment}"
    BillingMode: PAY_PER_REQUEST
    AttributeDefinitions:
      - AttributeName: pk
        AttributeType: S
      - AttributeName: sk
        AttributeType: S
      - AttributeName: gsi1pk
        AttributeType: S
      - AttributeName: gsi1sk
        AttributeType: S
    KeySchema:
      - AttributeName: pk
        KeyType: HASH
      - AttributeName: sk
        KeyType: RANGE
    GlobalSecondaryIndexes:
      - IndexName: GSI1_OUTBOX_BY_AGGREGATE
        KeySchema:
          - AttributeName: gsi1pk
            KeyType: HASH
          - AttributeName: gsi1sk
            KeyType: RANGE
        Projection:
          ProjectionType: ALL
    TimeToLiveSpecification:
      AttributeName: ttl
      Enabled: true
    PointInTimeRecoverySpecification:
      PointInTimeRecoveryEnabled: true
    SSESpecification:
      SSEEnabled: true
    Tags:
      - Key: Project
        Value: ffresco
      - Key: Environment
        Value: !Ref Environment
```

---

## 13. Environment variables

Core table:

```yaml
Environment:
  Variables:
    FFRESCO_CORE_TABLE_NAME: !Ref FfrescoCoreTable
```

Outbox table futura:

```yaml
Environment:
  Variables:
    FFRESCO_OUTBOX_TABLE_NAME: !Ref FfrescoOutboxTable
```

---

## 14. Permissions

Para MVP:

```yaml
Policies:
  - DynamoDBCrudPolicy:
      TableName: !Ref FfrescoCoreTable
```

Para producción, migrar a policy custom:

```txt
dynamodb:GetItem
dynamodb:PutItem
dynamodb:UpdateItem
dynamodb:DeleteItem
dynamodb:Query
dynamodb:BatchGetItem
dynamodb:TransactWriteItems
```

Para Outbox futura:

```txt
dynamodb:GetItem
dynamodb:PutItem
dynamodb:UpdateItem
dynamodb:Query
```

---

## 15. Reglas para Claude Code / Cursor

Cuando se implemente una feature:

```txt
1. Revisar access patterns antes de crear o modificar keys.
2. No crear Scan.
3. No inventar nuevos GSIs sin justificar el access pattern.
4. Mantener tenantId en keys principales o índices.
5. Usar GSI1 solo para public lookup / reverse lookup.
6. Usar GSI2 para work queues operativas por branch/status.
7. No guardar precios enviados por frontend como fuente confiable.
8. Mantener el menú público como item materializado para MVP.
9. Filtrar available=false en endpoints públicos.
10. No mezclar lógica de negocio en adapters DynamoDB.
```

---

## 16. Resumen de decisiones actuales

```txt
Core table:
ffresco-core-${Environment}

Main keys:
pk
sk

Initial GSIs:
GSI1_PUBLIC_LOOKUP
GSI2_BRANCH_WORK_QUEUE

MVP public menu:
Single materialized item:
PK = TENANT#<tenantId>#BRANCH#<branchId>
SK = MENU#PUBLIC

Public table lookup:
GSI1PK = PUBLIC_TABLE#<tablePublicId>
GSI1SK = TENANT#<tenantId>#BRANCH#<branchId>#TABLE#<tableId>

Orders:
PK = TENANT#<tenantId>#BRANCH#<branchId>#SESSION#<tableSessionId>
SK = ORDER#<createdAt>#<orderId>

Kitchen/work queues:
GSI2PK = TENANT#<tenantId>#BRANCH#<branchId>#WORK#ORDER#STATUS#<status>
GSI2SK = <createdAt>#<orderId>

Outbox:
No implementar al inicio salvo necesidad real.
Cuando evolucione, preferir tabla separada:
ffresco-outbox-${Environment}
```
