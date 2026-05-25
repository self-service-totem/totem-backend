# FFresco — Masterplan diario de PRs backend

## Objetivo del documento

Este documento es el **masterplan operativo** para construir el backend de FFresco PR por PR.

La idea es que cada día puedas tomar **1 o 2 PRs**, crear su ticket en Jira, implementarlos con IA y validar que el avance siga siempre el mismo estándar.

Este archivo responde principalmente estas preguntas:

```txt
1. ¿Qué PR hago ahora?
2. ¿A qué épica pertenece?
3. ¿Qué funcionalidad le da al usuario o al negocio?
4. ¿Qué endpoint o use case voy a implementar?
5. ¿Qué tengo que probar?
6. ¿Qué flujo completo queda habilitado cuando termino?
```

Este documento **no reemplaza** los documentos canónicos de arquitectura, testing, API contract o modelo de datos. Solo los referencia.

---

## Documentos canónicos que debe seguir cada PR

Cada PR de este masterplan debe respetar estos documentos:

```txt
CLAUDE.md
docs/01-architecture-standard.md
docs/02-testing-strategy.md
docs/03-data-model.md
docs/04-api-contract-standard.md
docs/05-local-aws-runbook.md
docs/docs/tickets/pr-ticket-template.md
docs/docs/tickets/prompt-create-ticket.md
.claude/commands/create-pr.md
```

Regla principal:

```txt
Este masterplan dice QUÉ construir y en qué orden.
Los docs canónicos dicen CÓMO construirlo.
El ticket dice el alcance exacto de una PR.
Claude Code implementa siguiendo el ticket + los docs canónicos.
```

---

## Convención de versionado de API

Todos los endpoints HTTP del backend deben usar versionado por path.

Formato:

```txt
/{apiVersion}/{domain}/{resource}
```

Versión actual:

```txt
/v1
```

Ejemplos:

```http
GET /v1/public/menu?tableId={tablePublicId}
POST /v1/public/tables/{tablePublicId}/orders
GET /v1/operations/branches/{branchId}/orders
POST /v1/admin/branches/{branchId}/menus
POST /v1/payments/orders/{orderId}/payment-intents
```

Regla:

```txt
El prefijo /v1 debe estar presente en:
- OpenAPI paths
- API Gateway routes
- tickets Jira
- acceptance-style tests
- ejemplos curl/Postman
```

La regla canónica debe vivir en:

```txt
docs/04-api-contract-standard.md
```

Este masterplan ya expresa los endpoints usando `/v1`.


---

## Regla de trabajo

```txt
Epic = dominio / futura Lambda
Story o Task = feature endpoint o grupo chico de endpoints
PR = implementación vertical revisable
```

Hoy la implementación puede vivir en una sola Lambda, pero el código debe quedar separado por dominio para poder extraerlo mañana en Lambdas independientes.

Cada PR debe ser una **vertical slice**:

```txt
API Gateway / OpenAPI
-> Router mapping
-> Spring Function / handler interno
-> Use case
-> Ports
-> DynamoDB adapter
-> Response DTO
-> Tests unitarios
-> Tests acceptance-style
```

---

## Rutina diaria sugerida

### Paso 1 — Elegir el PR del día

Elegir uno de los PRs de este documento siguiendo el orden recomendado.

Idealmente:

```txt
Día liviano    -> 1 PR
Día con foco   -> 2 PRs chicos
Día complejo   -> 1 PR + refactor/test cleanup
```

---

### Paso 2 — Crear ticket Jira

Usar:

```txt
docs/tickets/prompt-create-ticket.md
docs/tickets/pr-ticket-template.md
```

Entrada humana mínima:

```txt
Quiero crear el ticket para PR X.
Usá el masterplan.
Agregá Gherkin.
No repitas arquitectura.
Referenciá los docs canónicos.
```

Salida esperada:

```txt
Ticket listo para pegar en Jira
Branch sugerido
Scope
Out of scope
Gherkin
Acceptance Criteria
Testing Requirements
Definition of Done
```

---

### Paso 3 — Crear branch

Convención recomendada:

```txt
<type>/<ticket-id>-<short-description>
```

Ejemplos:

```txt
feature/11-bootstrap-demo
feature/1-public-menu
feature/2-place-order
fix/29-api-error-standardization
```

Usar `kebab-case`, no guiones bajos.

---

### Paso 4 — Implementar con Claude Code

Usar el comando:

```txt
/create-pr
```

Pasarle el ticket completo.

Claude debe implementar siguiendo:

```txt
CLAUDE.md
docs/01-architecture-standard.md
docs/02-testing-strategy.md
docs/03-data-model.md
docs/04-api-contract-standard.md
```

---

### Paso 5 — Validar

Antes de cerrar el PR, verificar:

```txt
1. Compila
2. Tests pasan
3. Endpoint está en OpenAPI/API Gateway
4. Router no tiene lógica de negocio
5. Use case orquesta
6. DynamoDB solo se usa desde adapters
7. Response sigue JSON:API/API contract acordado
8. Tests unitarios cubren reglas principales
9. Tests acceptance-style cubren el endpoint
10. No se modificaron archivos no relacionados
```

---

# Orden recomendado de construcción del MVP

## Fase 1 — Demo funcional mínima

Objetivo: poder cargar datos demo y mostrar un menú público.

```txt
PR 11 — Bootstrap Demo
PR 1  — Public Menu
```

Resultado:

```txt
El sistema puede crear una tienda demo, una sucursal, una mesa, productos, precios y un menú publicado.
Un cliente puede escanear un QR o abrir una URL pública y ver productos disponibles con sus precios.
```

---

## Fase 2 — Pedido y cocina

Objetivo: que un cliente pueda pedir y cocina pueda ver/preparar el pedido.

```txt
PR 2 — Place Order
PR 6 — Kitchen Orders List
PR 7 — Update Order Status
PR 3 — Customer Orders
```

Resultado:

```txt
El cliente crea un pedido desde el menú.
El backend valida productos y calcula precios.
La cocina ve pedidos pendientes.
La cocina cambia el estado del pedido.
El cliente puede consultar sus pedidos.
```

---

## Fase 3 — Cuenta, mozo y operación de mesa

Objetivo: cerrar el flujo de restaurante básico.

```txt
PR 4  — Bill
PR 5  — Waiter Calls
PR 9  — Operations Waiter Calls
PR 10 — Bill Close Requests
PR 8  — Order Display
```

Resultado:

```txt
El cliente puede ver su cuenta, pedir cerrar la mesa y llamar al mozo.
Operaciones puede ver llamados, solicitudes de cierre y pantalla de pedidos.
```

---

## Fase 4 — Administración mínima

Objetivo: que el restaurante pueda administrar su estructura base.

```txt
PR 12 — Tenant Management
PR 13 — Branch Management
PR 14 — Tables and Kiosks
```

Resultado:

```txt
El sistema permite crear tenants, sucursales, mesas y tótems.
Cada mesa puede tener un identificador público para QR.
```

---

## Fase 5 — Catálogo y precios

Objetivo: administrar menú, productos y precios reales.

```txt
PR 15 — Menu Management
PR 16 — Categories
PR 17 — Products
PR 18 — Price Lists
PR 19 — Price List Items
PR 20 — Discounts
PR 21 — Activate Price List
```

Resultado:

```txt
El restaurante puede crear menús, categorías, productos y listas de precios.
Al activar una lista de precios se regenera la proyección pública del menú.
El frontend público consulta una versión lista para mostrar.
```

---

## Fase 6 — Stock

Objetivo: consultar y ajustar stock por sucursal.

```txt
PR 22 — Stock Query
PR 23 — Stock Update / Adjustment
PR 24 — Stock Movements and Alerts
PR 25 — Stock Reservations
```

Resultado:

```txt
El sistema permite consultar stock, registrar movimientos, detectar productos bajo mínimo y reservar/liberar stock al operar pedidos.
```

---

## Fase 7 — Pagos

Objetivo: preparar el flujo de pagos e integración con proveedor externo.

```txt
PR 26 — Payment Intent
PR 27 — Payment Lifecycle
PR 28 — Mercado Pago Webhook
```

Resultado:

```txt
El sistema puede crear una intención de pago para una orden, consultar estado, confirmar/cancelar/reembolsar y recibir webhooks.
```

---

## Fase 8 — Notificaciones

Objetivo: desacoplar comunicaciones internas y externas.

```txt
PR 29 — Notification Model and Persistence
PR 30 — Notification Processing
PR 31 — Notification Senders
PR 32 — Notification Retries and DLQ
```

Resultado:

```txt
Eventos del sistema pueden generar notificaciones internas o externas sin acoplar los dominios de negocio a WhatsApp, Email, Push, WebSocket o cocina.
```

---

## Fase 9 — Calidad transversal

Objetivo: estandarizar errores, observabilidad y patrón de tests.

```txt
PR 33 — API Error Standardization
PR 34 — Observability
PR 35 — Acceptance Test Pattern
```

Resultado:

```txt
Todas las APIs responden errores de forma consistente, generan logs/metrics útiles y tienen un patrón común de acceptance-style tests.
```

---

# Épica 1 — Public / Customer API

## Objetivo

Endpoints públicos consumidos por el menú digital, QR de mesa y tótem.

```txt
Path agrupador: /v1/public/**
Paquete sugerido: com.ffresco.publicapi
Futura Lambda: ffresco-public-api
```

---

## PR 1 — Public Menu

```http
GET /v1/public/menu?tableId={tablePublicId}
GET /v1/public/menu/products/{productId}?tableId={tablePublicId}
```

### Funcionalidad para el usuario

El cliente escanea el QR de una mesa o abre el menú desde el tótem.  
El frontend llama al endpoint público con el `tablePublicId`.  
El backend identifica la mesa, la sucursal, el tenant y el menú activo.  
Luego devuelve al frontend la lista de categorías, productos disponibles y precios vigentes.

En lenguaje humano:

```txt
El cliente entra al menú.
El sistema reconoce desde qué mesa o tótem está entrando.
El sistema busca el menú publicado de esa sucursal.
El frontend muestra productos, categorías, imágenes y precios.
```

### Use cases

```txt
GetPublicMenuUseCase
GetPublicProductDetailUseCase
```

### Implementar

```txt
- Paquete publicapi.menu
- Router para /v1/public/menu
- Lectura de tablePublicId
- Resolución de mesa pública
- Lectura de menú público activo
- Response para frontend público
- OpenAPI/API Gateway route
```

### Probar

```txt
- 200 cuando tableId existe y hay menú activo
- 400 cuando falta tableId
- 404 cuando la mesa no existe
- 409 cuando la mesa está inactiva
- 404 cuando no hay menú activo
- Response incluye context, categories y products
```

---

## PR 2 — Place Order

```http
POST /v1/public/tables/{tablePublicId}/orders
```

### Funcionalidad para el usuario

El cliente selecciona productos desde el menú público y confirma el pedido.  
El backend no confía en los precios enviados por el frontend.  
Resuelve la mesa, busca o crea una sesión abierta, valida productos contra el menú activo, calcula precios en backend y crea la orden.

En lenguaje humano:

```txt
El cliente toca “pedir”.
El sistema valida que la mesa exista y esté abierta.
El sistema recalcula precios para evitar manipulación del frontend.
El pedido queda registrado para que cocina lo vea.
```

### Use case

```txt
PlaceOrderUseCase
```

### Implementar

```txt
- Paquete publicapi.order
- Router POST /v1/public/tables/{tablePublicId}/orders
- Resolución de mesa pública
- Buscar o crear TableSession OPEN
- Validación de productos
- Cálculo de precios
- Persistencia de ORDER
- Índice para cocina por branch/status
```

### Probar

```txt
- 201 pedido creado correctamente
- 400 payload inválido
- 404 mesa inexistente
- 409 mesa inactiva
- 409 producto no disponible
- El precio persistido sale del backend, no del request
- La orden queda consultable por cocina
```

---

## PR 3 — Customer Orders

```http
GET /v1/public/tables/{tablePublicId}/orders
```

### Funcionalidad para el usuario

El cliente puede ver los pedidos realizados en su mesa durante la sesión actual.  
Esto permite mostrar el estado de lo pedido: recibido, en preparación, listo, entregado o cancelado.

En lenguaje humano:

```txt
El cliente abre “mis pedidos”.
El sistema busca la sesión abierta de la mesa.
El frontend muestra el historial de pedidos de esa mesa.
```

### Use case

```txt
ListTableOrdersUseCase
```

### Implementar

```txt
- Reutilizar publicapi.order
- Leer órdenes de TableSession OPEN
- Filtro opcional por customerName
- Response para frontend customer
```

### Probar

```txt
- 200 lista de pedidos de la mesa
- 200 lista vacía si no hay pedidos
- 400 tablePublicId inválido
- 404 mesa inexistente
- No devolver pedidos de otra mesa
```

---

## PR 4 — Bill

```http
GET /v1/public/tables/{tablePublicId}/bill
POST /v1/public/tables/{tablePublicId}/bill/close-request
```

### Funcionalidad para el usuario

El cliente puede consultar la cuenta de su mesa o su cuenta individual y pedir cerrar la cuenta.  
El backend calcula totales usando las órdenes válidas de la sesión abierta e ignora órdenes canceladas.

En lenguaje humano:

```txt
El cliente toca “ver cuenta”.
El sistema calcula lo consumido.
El cliente puede ver su parte o la mesa completa.
El cliente pide cerrar la cuenta y operaciones recibe la solicitud.
```

### Use cases

```txt
GetTableBillUseCase
RequestBillCloseUseCase
```

### Implementar

```txt
- Paquete publicapi.bill
- Cálculo de cuenta por cliente
- Cálculo total de mesa
- Soporte scope = MINE | TABLE
- Crear BILL_CLOSE_REQUEST
```

### Probar

```txt
- 200 cuenta calculada correctamente
- Órdenes canceladas no suman
- scope MINE calcula solo un cliente
- scope TABLE calcula toda la mesa
- 201 solicitud de cierre creada
- 404 mesa inexistente
```

---

## PR 5 — Waiter Calls

```http
POST /v1/public/tables/{tablePublicId}/waiter-calls
```

### Funcionalidad para el usuario

El cliente puede llamar al mozo desde el menú digital.  
El backend registra un llamado con motivo y estado abierto para que operaciones lo vea.

En lenguaje humano:

```txt
El cliente toca “llamar mozo”.
El sistema crea un llamado asociado a la mesa.
Operaciones ve ese llamado en su pantalla.
```

### Use case

```txt
CreateWaiterCallUseCase
```

### Implementar

```txt
- Paquete publicapi.waiter
- Validar reason
- Crear WAITER_CALL
- Índice para operaciones por branch/status
```

### Probar

```txt
- 201 llamado creado
- 400 reason inválido
- 404 mesa inexistente
- El llamado aparece para operaciones
```

---

# Épica 2 — Operations / Kitchen API

## Objetivo

Endpoints internos para cocina, pantalla de pedidos, mozo y caja.

```txt
Path agrupador: /v1/operations/**
Paquete sugerido: com.ffresco.operations
Futura Lambda: ffresco-operations-api
```

---

## PR 6 — Kitchen Orders List

```http
GET /v1/operations/branches/{branchId}/orders
```

### Funcionalidad para el usuario

La cocina puede ver los pedidos que debe preparar para una sucursal.  
El frontend de cocina consulta pedidos por branch y status.

En lenguaje humano:

```txt
Cocina abre su pantalla.
El sistema consulta pedidos pendientes o en preparación.
La pantalla muestra qué preparar y en qué orden.
```

### Use case

```txt
ListKitchenOrdersUseCase
```

### Implementar

```txt
- Paquete operations.orders
- Query por branchId y status
- Uso de GSI por branch/status
- Response para KDS/cocina
```

### Probar

```txt
- 200 pedidos por status
- 200 lista vacía
- No mezclar pedidos de otra sucursal
- Orden correcto por fecha o prioridad definida
```

---

## PR 7 — Update Order Status

```http
PATCH /v1/operations/orders/{orderId}/status
```

### Funcionalidad para el usuario

La cocina o el operador puede cambiar el estado de un pedido.  
Por ejemplo: recibido, en preparación, listo, entregado o cancelado.

En lenguaje humano:

```txt
Cocina marca un pedido como “preparando”.
Luego lo marca como “listo”.
El cliente y/o la pantalla pública pueden ver el avance.
```

### Use case

```txt
UpdateOrderStatusUseCase
```

### Implementar

```txt
- Validar estado permitido
- Validar transición de estado
- Actualizar status
- Actualizar claves/índices de búsqueda por status
- Guardar updatedAt
```

### Probar

```txt
- 200 status actualizado
- 400 status inválido
- 404 order inexistente
- 409 transición no permitida
- El pedido cambia de lista al consultar por status
```

---

## PR 8 — Order Display

```http
GET /v1/operations/branches/{branchId}/order-display
```

### Funcionalidad para el usuario

Una pantalla pública o interna muestra pedidos en preparación y listos.  
Sirve para que clientes o personal vean el avance sin entrar al detalle operativo.

En lenguaje humano:

```txt
La pantalla de pedidos consulta al backend.
El sistema devuelve columnas como “en preparación” y “listos”.
El display se actualiza con el estado actual de cada orden.
```

### Use case

```txt
GetOrderDisplayUseCase
```

### Implementar

```txt
- Paquete operations.display
- Leer pedidos PREPARING y READY
- Devolver columnas para pantalla
```

### Probar

```txt
- 200 display con columnas
- No incluir pedidos cancelados
- No incluir pedidos de otra sucursal
```

---

## PR 9 — Operations Waiter Calls

```http
GET /v1/operations/branches/{branchId}/waiter-calls
PATCH /v1/operations/waiter-calls/{waiterCallId}/status
```

### Funcionalidad para el usuario

Operaciones puede ver y gestionar llamados de mozo generados desde las mesas.

En lenguaje humano:

```txt
El cliente llama al mozo.
Operaciones ve el llamado.
Un operador lo toma, lo marca en progreso y luego terminado.
```

### Use cases

```txt
ListWaiterCallsUseCase
UpdateWaiterCallStatusUseCase
```

### Implementar

```txt
- Paquete operations.waiter
- Leer llamados por branch/status
- Actualizar estado OPEN | IN_PROGRESS | DONE | CANCELLED
```

### Probar

```txt
- 200 lista de llamados abiertos
- 200 status actualizado
- 404 llamado inexistente
- 409 transición inválida
```

---

## PR 10 — Bill Close Requests

```http
GET /v1/operations/branches/{branchId}/bill-close-requests
PATCH /v1/operations/bill-close-requests/{requestId}/status
```

### Funcionalidad para el usuario

Caja u operaciones puede ver solicitudes de cierre de cuenta y marcarlas como atendidas.

En lenguaje humano:

```txt
El cliente pide cerrar la cuenta.
Caja ve la solicitud.
Caja la procesa y actualiza el estado.
```

### Use cases

```txt
ListBillCloseRequestsUseCase
UpdateBillCloseRequestStatusUseCase
```

### Implementar

```txt
- Paquete operations.bill
- Leer solicitudes abiertas
- Actualizar status
```

### Probar

```txt
- 200 solicitudes abiertas
- 200 status actualizado
- 404 solicitud inexistente
- No mezclar sucursales
```

---

# Épica 3 — Admin / Restaurant API

## Objetivo

Administración de tenant, sucursales, mesas y tótems.

```txt
Path agrupador: /v1/admin/**
Paquete sugerido: com.ffresco.admin.restaurant
Futura Lambda: ffresco-admin-api
```

---

## PR 11 — Bootstrap Demo

```http
POST /v1/admin/dev/bootstrap-demo
```

### Funcionalidad para el usuario

El desarrollador o demo environment puede crear datos iniciales para probar el flujo completo sin cargar todo manualmente.

En lenguaje humano:

```txt
Ejecuto bootstrap.
El sistema crea tenant, branch, mesa, tótem, menú, categorías, productos, lista de precios y menú publicado.
Después puedo abrir el menú público y probar el flujo.
```

### Use case

```txt
BootstrapDemoUseCase
```

### Implementar

```txt
- Paquete admin.bootstrap
- Crear tenant demo
- Crear branch B001
- Crear mesa 001
- Crear tótem demo
- Crear menú demo
- Crear categorías y productos demo
- Crear lista de precios
- Publicar menú demo
```

### Probar

```txt
- 201 demo creado
- Re-ejecución idempotente o comportamiento definido
- Los datos creados permiten usar PR 1 Public Menu
- No rompe datos existentes
```

---

## PR 12 — Tenant Management

```http
POST /v1/admin/tenants
GET /v1/admin/tenants/{tenantId}
```

### Funcionalidad para el usuario

El administrador de la plataforma puede crear y consultar restaurantes/negocios clientes.

En lenguaje humano:

```txt
Creo un nuevo restaurante en la plataforma.
El sistema guarda su información base.
Luego puedo consultarlo por id.
```

### Use cases

```txt
CreateTenantUseCase
GetTenantUseCase
```

### Implementar

```txt
- Paquete admin.restaurant.tenant
- Crear tenant
- Consultar tenant
- Validar slug único
```

### Probar

```txt
- 201 tenant creado
- 200 tenant consultado
- 400 datos inválidos
- 409 slug duplicado
- 404 tenant inexistente
```

---

## PR 13 — Branch Management

```http
POST /v1/admin/tenants/{tenantId}/branches
GET /v1/admin/branches/{branchId}
```

### Funcionalidad para el usuario

El administrador puede crear sucursales para un tenant.

En lenguaje humano:

```txt
Un restaurante puede tener una o varias sucursales.
Creo una sucursal asociada al tenant.
Luego puedo consultarla.
```

### Use cases

```txt
CreateBranchUseCase
GetBranchUseCase
```

### Implementar

```txt
- Paquete admin.restaurant.branch
- Crear branch asociada al tenant
- Consultar branch
```

### Probar

```txt
- 201 branch creada
- 200 branch consultada
- 404 tenant inexistente
- 404 branch inexistente
- No crear branch sin tenant válido
```

---

## PR 14 — Tables and Kiosks

```http
POST /v1/admin/branches/{branchId}/tables
GET /v1/admin/branches/{branchId}/tables
PATCH /v1/admin/tables/{tableId}
```

### Funcionalidad para el usuario

El administrador puede crear mesas y tótems asociados a una sucursal.  
Las mesas tienen identificador público para QR. Los tótems pueden usar otro flujo.

En lenguaje humano:

```txt
Creo mesas para una sucursal.
El sistema genera un tablePublicId.
Con ese id se arma el QR del menú.
También puedo crear dispositivos tipo tótem.
```

### Use cases

```txt
CreateTableUseCase
ListBranchTablesUseCase
UpdateTableUseCase
```

### Implementar

```txt
- Paquete admin.restaurant.table
- Soportar type = TABLE | KIOSK
- Generar tablePublicId
- Generar QR URL si es mesa
- No generar QR URL si es tótem
```

### Probar

```txt
- 201 mesa creada con QR URL
- 201 tótem creado sin QR URL
- 200 lista de mesas por branch
- 200 mesa actualizada
- 404 branch inexistente
```

---

# Épica 4 — Catalog API

## Objetivo

Gestión de menú, categorías y productos.

```txt
Path agrupador: /v1/admin/menus/**, /v1/admin/products/**, /v1/admin/categories/**
Paquete sugerido: com.ffresco.catalog
Futura Lambda: ffresco-catalog-api
```

---

## PR 15 — Menu Management

```http
POST /v1/admin/branches/{branchId}/menus
GET /v1/admin/branches/{branchId}/menus
POST /v1/admin/branches/{branchId}/menus/{menuId}/publish
```

### Funcionalidad para el usuario

El administrador puede crear menús para una sucursal y publicar uno como activo.

En lenguaje humano:

```txt
Creo un menú para una sucursal.
Agrego categorías y productos.
Cuando lo publico, el menú queda disponible para clientes.
```

### Use cases

```txt
CreateMenuUseCase
ListBranchMenusUseCase
PublishMenuUseCase
```

### Implementar

```txt
- Paquete catalog.menu
- Crear menú
- Listar menús por branch
- Publicar menú como activeMenuId
- Actualizar proyección PUBLIC_MENU
```

### Probar

```txt
- 201 menú creado
- 200 menús listados
- 200 menú publicado
- 404 branch inexistente
- 404 menu inexistente
- Publicar actualiza menú público
```

---

## PR 16 — Categories

```http
POST /v1/admin/menus/{menuId}/categories
PATCH /v1/admin/categories/{categoryId}
```

### Funcionalidad para el usuario

El administrador puede organizar el menú en categorías como bebidas, entradas, platos o postres.

En lenguaje humano:

```txt
Creo categorías dentro de un menú.
El frontend público usa esas categorías para ordenar los productos.
```

### Use cases

```txt
CreateCategoryUseCase
UpdateCategoryUseCase
```

### Implementar

```txt
- Paquete catalog.category
- Crear categoría
- Actualizar categoría
- Mantener orden de categorías
```

### Probar

```txt
- 201 categoría creada
- 200 categoría actualizada
- 404 menú inexistente
- Orden se mantiene correctamente
```

---

## PR 17 — Products

```http
POST /v1/admin/menus/{menuId}/products
GET /v1/admin/products/{productId}
PATCH /v1/admin/products/{productId}
```

### Funcionalidad para el usuario

El administrador puede crear, consultar y actualizar productos del menú.

En lenguaje humano:

```txt
Creo un producto.
Lo asocio a una categoría.
Marco si está activo y disponible.
El menú público solo debe mostrar lo que corresponda.
```

### Use cases

```txt
CreateProductUseCase
GetProductUseCase
UpdateProductUseCase
```

### Implementar

```txt
- Paquete catalog.product
- Crear producto
- Consultar producto
- Actualizar producto
- Soportar activo/inactivo
- Soportar disponible/no disponible
```

### Probar

```txt
- 201 producto creado
- 200 producto consultado
- 200 producto actualizado
- 404 producto inexistente
- Producto inactivo/no disponible no debe mostrarse como disponible en menú público
```

---

# Épica 5 — Pricing API

## Objetivo

Listas de precios, precios de venta, descuentos y activación.

```txt
Path agrupador: /v1/admin/price-lists/**
Paquete sugerido: com.ffresco.pricing
Futura Lambda: ffresco-pricing-api
```

---

## PR 18 — Price Lists

```http
POST /v1/admin/branches/{branchId}/price-lists
GET /v1/admin/branches/{branchId}/price-lists
GET /v1/admin/price-lists/{priceListId}
```

### Funcionalidad para el usuario

El administrador puede crear y consultar listas de precios para una sucursal.

En lenguaje humano:

```txt
Creo una lista de precios para una sucursal.
Defino moneda y vigencia.
Luego puedo cargar precios por producto.
```

### Use cases

```txt
CreatePriceListUseCase
ListPriceListsUseCase
GetPriceListUseCase
```

### Implementar

```txt
- Paquete pricing.pricelist
- Crear lista de precios
- Listar listas por branch
- Obtener lista por id
- Soportar nombre, moneda y vigencia
```

### Probar

```txt
- 201 lista creada
- 200 listas por sucursal
- 200 lista por id
- 404 branch inexistente
- Validación de moneda/vigencia
```

---

## PR 19 — Price List Items

```http
POST /v1/admin/price-lists/{priceListId}/items
PATCH /v1/admin/price-lists/{priceListId}/items/{productId}
```

### Funcionalidad para el usuario

El administrador carga o actualiza el precio de venta de cada producto.

En lenguaje humano:

```txt
Selecciono un producto.
Le asigno precio de venta.
El sistema guarda el precio en una lista.
Cuando la lista se active, ese precio aparecerá en el menú público.
```

### Use cases

```txt
AddPriceListItemUseCase
UpdatePriceListItemUseCase
```

### Implementar

```txt
- Agregar item a lista de precios
- Actualizar item de lista de precios
- Guardar precio de costo opcional
- Guardar precio de venta
- Validar precio mayor o igual a cero
```

### Probar

```txt
- 201 item agregado
- 200 item actualizado
- 400 precio negativo
- 404 priceList inexistente
- 404 product inexistente
```

---

## PR 20 — Discounts

```http
POST /v1/admin/price-lists/{priceListId}/discounts
PATCH /v1/admin/price-lists/{priceListId}/discounts/{discountId}
```

### Funcionalidad para el usuario

El administrador puede crear descuentos por producto, categoría o lista completa.

En lenguaje humano:

```txt
Creo una promoción.
Puede aplicar a un producto, una categoría o toda la lista.
El sistema valida vigencia y tipo de descuento.
```

### Use cases

```txt
CreateDiscountUseCase
UpdateDiscountUseCase
```

### Implementar

```txt
- Paquete pricing.discount
- Descuento por producto/categoría/lista
- Porcentaje o monto fijo
- Validar vigencia
```

### Probar

```txt
- 201 descuento creado
- 200 descuento actualizado
- 400 porcentaje inválido
- 400 vigencia inválida
- 404 priceList inexistente
```

---

## PR 21 — Activate Price List

```http
POST /v1/admin/branches/{branchId}/price-lists/{priceListId}/activate
```

### Funcionalidad para el usuario

El administrador activa una lista de precios para una sucursal.  
Al activarla, el sistema regenera la proyección pública del menú con precios vigentes.

En lenguaje humano:

```txt
Activo una lista de precios.
El sistema recalcula el menú público.
El cliente ve productos con los nuevos precios.
```

### Use case

```txt
ActivatePriceListUseCase
```

### Implementar

```txt
- Activar lista de precios en sucursal
- Validar que pertenezca a la branch
- Regenerar PUBLIC_MENU con precios vigentes
```

### Probar

```txt
- 200 lista activada
- 404 branch inexistente
- 404 priceList inexistente
- 409 lista no pertenece a la branch
- Menú público refleja precios activados
```

---

# Épica 6 — Stock API

## Objetivo

Stock por sucursal, movimientos, reservas y alertas.

```txt
Path agrupador: /v1/admin/branches/{branchId}/stock/**
Paquete sugerido: com.ffresco.stock
Futura Lambda: ffresco-stock-api
```

---

## PR 22 — Stock Query

```http
GET /v1/admin/branches/{branchId}/stock
GET /v1/admin/branches/{branchId}/stock/{productId}
```

### Funcionalidad para el usuario

El administrador consulta el stock disponible de una sucursal o de un producto concreto.

En lenguaje humano:

```txt
El frontend de administración abre la pantalla de stock.
El sistema consulta los productos de la sucursal.
El frontend muestra cantidades disponibles, mínimas y estado.
```

### Use cases

```txt
ListStockUseCase
GetProductStockUseCase
```

### Implementar

```txt
- Paquete stock.query
- Leer stock por branch/product
- Response para pantalla de stock
```

### Probar

```txt
- 200 lista de stock
- 200 stock de producto
- 404 producto sin stock o inexistente
- No mezclar stock entre sucursales
```

---

## PR 23 — Stock Update / Adjustment

```http
PATCH /v1/admin/branches/{branchId}/stock/{productId}
POST /v1/admin/branches/{branchId}/stock-adjustments
```

### Funcionalidad para el usuario

El administrador puede ajustar stock manualmente por reposición, pérdida, corrección o inventario.

En lenguaje humano:

```txt
El operador corrige el stock de un producto.
El sistema actualiza la cantidad.
También registra un movimiento para auditoría.
```

### Use cases

```txt
UpdateStockUseCase
CreateStockAdjustmentUseCase
```

### Implementar

```txt
- Paquete stock.adjustment
- Actualizar cantidad
- Registrar movimiento de stock
- Validar cantidades
```

### Probar

```txt
- 200 stock actualizado
- 201 ajuste creado
- 400 cantidad inválida
- Movimiento queda registrado
```

---

## PR 24 — Stock Movements and Alerts

```http
GET /v1/admin/branches/{branchId}/stock-movements
GET /v1/admin/branches/{branchId}/stock-alerts
```

### Funcionalidad para el usuario

El administrador puede ver historial de movimientos y productos bajo mínimo.

En lenguaje humano:

```txt
El operador abre alertas de stock.
El sistema calcula productos por debajo del mínimo.
También puede revisar movimientos históricos.
```

### Use cases

```txt
ListStockMovementsUseCase
ListStockAlertsUseCase
```

### Implementar

```txt
- Paquete stock.movement
- Paquete stock.alert
- Listar movimientos
- Calcular productos bajo mínimo
```

### Probar

```txt
- 200 movimientos
- 200 alertas
- Productos bajo mínimo aparecen
- Productos normales no aparecen como alerta
```

---

## PR 25 — Stock Reservations

```http
POST /v1/admin/branches/{branchId}/stock-reservations
POST /v1/admin/branches/{branchId}/stock-releases
```

### Funcionalidad para el usuario

El sistema puede reservar stock cuando se crea un pedido y liberarlo si el pedido se cancela.

En lenguaje humano:

```txt
Cuando entra un pedido, el sistema reserva productos.
Si el pedido se cancela, libera la reserva.
Esto evita vender más de lo disponible.
```

### Use cases

```txt
ReserveStockUseCase
ReleaseStockUseCase
```

### Implementar

```txt
- Paquete stock.reservation
- Reservar stock
- Liberar stock
- Integración futura con orders
```

### Probar

```txt
- 201 reserva creada
- 201 liberación creada
- 409 stock insuficiente
- Stock disponible cambia correctamente
```

---

# Épica 7 — Payments API

## Objetivo

Pagos, payment intents, confirmaciones, cancelaciones, refunds y webhooks.

```txt
Path agrupador: /v1/payments/**
Paquete sugerido: com.ffresco.payments
Futura Lambda: ffresco-payments-api
```

---

## PR 26 — Payment Intent

```http
POST /v1/payments/orders/{orderId}/payment-intents
GET /v1/payments/orders/{orderId}
GET /v1/payments/orders/{orderId}/status
```

### Funcionalidad para el usuario

El cliente o frontend inicia el proceso de pago de una orden.  
El backend crea una intención de pago y permite consultar su estado.

En lenguaje humano:

```txt
El cliente quiere pagar.
El sistema crea un payment intent para la orden.
El frontend usa esa información para continuar el pago.
```

### Use cases

```txt
CreatePaymentIntentUseCase
GetOrderPaymentUseCase
GetOrderPaymentStatusUseCase
```

### Implementar

```txt
- Paquete payments.intent
- Crear PAYMENT
- Asociar pago a orden
- Consultar pago por orden
- Consultar status
```

### Probar

```txt
- 201 payment intent creado
- 200 pago consultado
- 200 status consultado
- 404 order inexistente
- 409 orden no pagable
```

---

## PR 27 — Payment Lifecycle

```http
GET /v1/payments/{paymentId}
POST /v1/payments/{paymentId}/confirm
POST /v1/payments/{paymentId}/cancel
POST /v1/payments/{paymentId}/refund
```

### Funcionalidad para el usuario

El sistema administra el ciclo de vida de un pago: consulta, confirmación, cancelación y reembolso.

En lenguaje humano:

```txt
Un pago puede confirmarse, cancelarse o reembolsarse.
El sistema valida que la transición tenga sentido.
```

### Use cases

```txt
GetPaymentUseCase
ConfirmPaymentUseCase
CancelPaymentUseCase
RefundPaymentUseCase
```

### Implementar

```txt
- Paquete payments.lifecycle
- Validar transiciones de estado
- Actualizar payment status
```

### Probar

```txt
- 200 pago consultado
- 200 pago confirmado
- 200 pago cancelado
- 200 pago reembolsado
- 409 transición inválida
```

---

## PR 28 — Mercado Pago Webhook

```http
POST /v1/payments/webhooks/mercado-pago
```

### Funcionalidad para el usuario

El sistema recibe eventos externos de Mercado Pago y actualiza pagos internos.

En lenguaje humano:

```txt
Mercado Pago avisa que un pago cambió de estado.
El backend recibe el webhook.
El sistema actualiza el payment correspondiente de forma idempotente.
```

### Use case

```txt
HandleMercadoPagoWebhookUseCase
```

### Implementar

```txt
- Paquete payments.webhook
- Validar payload
- Asociar evento externo
- Actualizar payment status
- Preparar idempotencia
```

### Probar

```txt
- 200 webhook aceptado
- 400 payload inválido
- Evento duplicado no rompe estado
- Payment status actualizado
```

---

# Épica 8 — Notifications API / Module

## Objetivo

Implementar un módulo de notificaciones desacoplado que permita generar, registrar, enviar y reintentar comunicaciones internas o externas originadas por eventos del sistema.

```txt
Paquete sugerido: com.ffresco.notification
Futura Lambda opcional: ffresco-notifications-worker
```

Regla de diseño:

```txt
Domain Event -> Notification -> Sender Adapter
```

Canales previstos:

```txt
KITCHEN_SCREEN
WHATSAPP
EMAIL
SMS
PUSH
WEBSOCKET
INTERNAL_EVENT
```

Estados previstos:

```txt
PENDING
PROCESSING
SENT
FAILED
CANCELED
```

---

## PR 29 — Notification Model and Persistence

### Funcionalidad para el usuario

El sistema puede registrar una notificación pendiente a partir de un evento del dominio.

En lenguaje humano:

```txt
Ocurre algo importante, por ejemplo OrderCreated.
El sistema crea una Notification.
La notificación queda pendiente para ser enviada.
```

### Use case

```txt
CreateNotificationUseCase
```

### Implementar

```txt
- Notification
- NotificationAttempt
- NotificationChannel
- NotificationStatus
- NotificationRecipientType
- DynamoNotificationRepository
- TTL expiresAt para notificaciones antiguas
```

### Probar

```txt
- Notification creada en PENDING
- Contiene tenantId, branchId, channel, recipient, templateCode y payload
- TTL presente
- No hay scans globales
```

---

## PR 30 — Notification Processing

### Funcionalidad para el usuario

El sistema puede procesar notificaciones pendientes de forma asíncrona.

En lenguaje humano:

```txt
Hay una notificación pendiente.
Un worker la toma.
La marca como PROCESSING.
Luego intenta enviarla por el canal correspondiente.
```

### Use cases

```txt
ProcessNotificationEventUseCase
SendNotificationUseCase
```

### Implementar

```txt
- SqsNotificationQueuePublisher
- SqsNotificationQueueConsumer
- Cambio PENDING -> PROCESSING
- Registro de intento
```

### Probar

```txt
- Mensaje publicado en SQS
- Consumer procesa mensaje
- NotificationAttempt creado
- No se procesa dos veces sin control
```

---

## PR 31 — Notification Senders

### Funcionalidad para el usuario

El sistema puede enviar notificaciones usando adapters separados por canal.

En lenguaje humano:

```txt
Una notificación puede ir a cocina, WhatsApp, email o WebSocket.
El dominio no sabe nada del proveedor.
Solo se conecta con un sender adapter.
```

### Use cases

```txt
SendNotificationUseCase
```

### Implementar

```txt
- Sender port
- WebSocketNotificationSender
- EmailNotificationSender
- WhatsAppNotificationSender placeholder
- InternalEventNotificationSender
```

### Probar

```txt
- Sender correcto según channel
- Éxito cambia status a SENT
- sentAt queda informado
- Error queda registrado
```

---

## PR 32 — Notification Retries and DLQ

### Funcionalidad para el usuario

El sistema puede reintentar notificaciones fallidas y enviar a DLQ si no se pueden procesar.

En lenguaje humano:

```txt
Si una notificación falla, no se pierde.
El sistema registra el error.
Puede reintentar.
Si sigue fallando, queda trazabilidad en DLQ.
```

### Use cases

```txt
RetryFailedNotificationUseCase
```

### Implementar

```txt
- Política de retry
- Incrementar attemptCount
- Guardar lastError
- Integración con DLQ
```

### Probar

```txt
- Fallo incrementa attemptCount
- Error queda en lastError
- Reintento exitoso cambia a SENT
- Reintentos agotados terminan en FAILED/DLQ
```

---

# Épica 9 — Cross-cutting / Platform Quality

## Objetivo

Reglas comunes para que todas las PRs mantengan calidad y sean fáciles de separar mañana.

No representa una Lambda futura, pero sí una épica técnica.

---

## PR 33 — API Error Standardization

### Funcionalidad para el usuario

Todas las APIs responden errores con el mismo formato y códigos consistentes.

En lenguaje humano:

```txt
Si algo falla, frontend y QA reciben siempre una respuesta predecible.
No hay errores distintos para cada endpoint.
```

### Implementar

```txt
- Error response común
- Mapeo dominio -> HTTP
- 400, 404, 409, 500
- Tests de error mapping
```

### Probar

```txt
- 400 input inválido
- 404 recurso inexistente
- 409 conflicto de negocio
- 500 error inesperado
- Formato consistente
```

---

## PR 34 — Observability

### Funcionalidad para el usuario

El equipo técnico puede diagnosticar errores y performance con logs, correlation IDs y métricas.

En lenguaje humano:

```txt
Cuando algo falla en producción, puedo seguir el request.
Puedo ver endpoint, tenant, branch, status y error.
```

### Implementar

```txt
- Logs estructurados
- Correlation ID
- Request ID
- Métricas básicas por endpoint
- Preparar CloudWatch/New Relic
```

### Probar

```txt
- Cada request tiene correlationId
- Errores loguean contexto útil
- No se loguean datos sensibles
- Métricas se emiten correctamente
```

---

## PR 35 — Acceptance Test Pattern

### Funcionalidad para el usuario

El proyecto gana un patrón común para probar endpoints completos desde router/function.

En lenguaje humano:

```txt
Cada nuevo endpoint se prueba de la misma manera.
La IA puede copiar el patrón.
Yo puedo revisar rápido si una feature está bien cubierta.
```

### Implementar

```txt
- Carpeta de tests acceptance-style
- Naming convention
- Fixture builder para DynamoDB
- Helper para ejecutar router/function
- Documentar estrategia
```

### Probar

```txt
- Test acceptance ejecuta endpoint desde router/function
- Fixture builder crea datos realistas
- El patrón queda documentado
```

---

# Checklist para cerrar cada día

Al terminar el día, actualizar este bloque manualmente o en un archivo de log separado.

```txt
Fecha:
PR trabajado:
Branch:
Ticket Jira:
Estado: TODO | IN_PROGRESS | DONE | BLOCKED

Qué implementé:
-

Qué probé:
-

Qué aprendí:
-

Qué falta:
-

Próximo PR sugerido:
-
```

---

# Mini formato para pedirle a ChatGPT crear el próximo ticket

```txt
Usá el masterplan FFresco.

Quiero crear el ticket para:
PR X — [nombre]

Necesito:
- título
- branch sugerido
- descripción humana
- scope
- out of scope
- Gherkin
- acceptance criteria
- testing requirements
- definition of done

No repitas arquitectura.
Referenciá los docs canónicos.
El ticket tiene que estar listo para pegar en Jira y luego pasarlo a Claude Code.
```

---

# Mini formato para pedirle a Claude Code implementar

```txt
/create-pr

Implement the Jira ticket below.

Follow:
- CLAUDE.md
- docs/01-architecture-standard.md
- docs/02-testing-strategy.md
- docs/03-data-model.md
- docs/04-api-contract-standard.md

Do not rewrite architecture.
Do not implement out-of-scope endpoints.
Do not put business logic in router.
Do not access DynamoDB outside adapters.

[TICKET HERE]
```
