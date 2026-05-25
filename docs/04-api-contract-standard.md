# API Contract Standard

## Principio

```text
OpenAPI first. JSON:API always.
```

OpenAPI define paths, methods, parameters, request bodies, responses y errores. El código debe seguir el contrato, no inventarlo.

## OpenAPI source strategy

No editar `openapi.yaml` bundleado a mano.

Flujo recomendado:

```text
1. Editar fragments en `openapi-src/`.
2. Registrar fragments en `openapi-src/openapi-root.yaml`.
3. Ejecutar bundle.
4. Usar el `openapi.yaml` generado para SAM/API Gateway.
```

## JSON:API content type

```http
Content-Type: application/vnd.api+json
Accept: application/vnd.api+json
```

## Success response

Single resource:

```json
{
  "data": {
    "type": "resource-type",
    "id": "resource-id",
    "attributes": {}
  }
}
```

Collection:

```json
{
  "data": [
    {
      "type": "resource-type",
      "id": "resource-id",
      "attributes": {}
    }
  ]
}
```

Regla:

```text
GET /resource/{id} o endpoint que apunta a un único recurso -> data object
GET /resources o endpoint de búsqueda/listado -> data array, incluso vacío []
```

## Request body

GET con id/path/query normalmente no necesita body.

POST/PATCH/PUT deben usar JSON:API document:

```json
{
  "data": {
    "type": "resource-type",
    "attributes": {}
  }
}
```

## Error response

```json
{
  "errors": [
    {
      "status": "400",
      "title": "Bad Request",
      "detail": "Error detail",
      "code": "STABLE_ERROR_CODE"
    }
  ]
}
```

`code` debe ser estable cuando el frontend o tests puedan depender de él.

## Error mapping

El mapeo de excepciones a JSON:API error debe estar centralizado:

```text
Lambda/API Gateway -> ApiExceptionHandler
Local web mode     -> JsonApiWebExceptionHandler
```

Las domain exceptions que necesiten código estable deben implementar una interfaz equivalente a:

```text
CodedDomainException
```

## API adapter rule

El HTTP adapter no devuelve domain objects ni application responses crudos.

Debe mapear siempre a JSON:API mediante:

```text
<Resource>JsonApiMapper
JsonApiResponseFactory
JsonApiDocument
JsonApiResource
JsonApiError
```


## API versioning convention

All HTTP endpoints must be versioned using path-based versioning.

Format:

`/{apiVersion}/{domain}/{resource}`

Current version:

`/v1`

Examples:

- `/v1/public/menu`
- `/v1/public/tables/{tablePublicId}/orders`
- `/v1/operations/branches/{branchId}/orders`
- `/v1/admin/branches/{branchId}/menus`
- `/v1/payments/orders/{orderId}/payment-intents`

The version must be part of the OpenAPI path and API Gateway route.


Rules:

```text
1. Do not create unversioned routes.
2. Do not place the version inside the domain path.
3. Tickets, acceptance-style tests and curl/Postman examples must use versioned paths.
```

Correct:

```http
/v1/public/menu
/v1/admin/branches/{branchId}/stock
```

Incorrect:

```http
/public/menu
/public/v1/menu
/admin/v1/branches/{branchId}/stock
```
