# Arquitectura - Lambda Spring Cloud Function + API Gateway + JSON:API

## Objetivo

Este proyecto queda armado como base para agregar endpoints de forma uniforme usando:

- Java 21
- Spring Cloud Function
- AWS Lambda
- API Gateway HTTP API v2
- SAM
- Arquitectura hexagonal
- Contrato de entrada/salida JSON:API en la capa de infraestructura

La regla principal es:

```text
JSON:API vive solamente en infrastructure.adapter.in.api.
Spring Cloud Function vive solamente en infrastructure.adapter.in.function.
Application y domain no conocen JSON:API, API Gateway, Lambda, Jackson ni Spring Cloud Function.
```

## Dirección de dependencias

```text
infrastructure -> application -> domain
```

El dominio queda independiente de frameworks y detalles técnicos.

## Flujo AWS / API Gateway

```text
API Gateway HTTP API v2
  -> Lambda
  -> FunctionInvoker
  -> apiGatewayRouter
  -> ApiGatewayRouterFunction
  -> ApiGatewayRouteHandler
  -> JsonApiMapper
  -> Spring Cloud Function adapter
  -> UseCase
  -> Domain
  -> Port out
  -> Adapter out
```

Ejemplo actual:

```text
GET /price-lists/{priceListId}
  -> apiGatewayRouter
  -> GetPriceListRouteHandler
  -> PriceListJsonApiMapper
  -> GetPriceListFunction
  -> GetPriceListUseCase
  -> GetPriceListService
  -> LoadProductsPort
  -> InMemoryProductAdapter
```

## Flujo local directo

`application.yml` se mantiene para pruebas locales de funciones directas con Spring Cloud Function Web.

Ejemplo:

```yaml
spring:
  cloud:
    function:
      definition: health
```

Esto permite probar una función directa local sin simular todo el payload de API Gateway.

Cuando se despliega con SAM/AWS, `template.yaml` usa:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

Por eso hay dos modos:

```text
Local directo:
  POST /health o POST /listPrice
  -> function bean directa

AWS/API Gateway:
  GET /health o GET /price-lists/{priceListId}
  -> apiGatewayRouter
  -> route handler
```

## Mapa de paquetes recomendado

```text
com.ffresco.pricelist

  domain
    model
    enums
    exception

  application
    port
      in
        pricelist
          GetPriceListUseCase
          GetPriceListCommand
      out
        LoadProductsPort
    service
      pricelist
        GetPriceListService

  infrastructure
    adapter
      in
        api
          ApiGatewayRouterFunction
          ApiGatewayRouteHandler
          ApiGatewayRequest
          ApiExceptionHandler
          JsonApiDocument
          JsonApiResource
          JsonApiError
          JsonApiResponseFactory

          health
            GetHealthRouteHandler
            HealthJsonApiMapper
            HealthAttributes

          pricelist
            GetPriceListRouteHandler
            PriceListJsonApiMapper
            PriceListAttributes
            PriceListProductAttributes

        function
          health
            HealthFunction
            HealthFunctionResponse

          pricelist
            GetPriceListFunction
            GetPriceListRequest
            GetPriceListResponse
            ProductResponse

      out
        memory
          InMemoryProductAdapter

    config
      ApiGatewayConfig
      HealthConfig
      PriceListConfig
      PersistenceConfig
      JacksonConfig
```

## Convenciones de nombres

### Application

Usar:

```text
<Action><Resource>UseCase
<Action><Resource>Command
<Action><Resource>Service
```

Ejemplo:

```text
GetPriceListUseCase
GetPriceListCommand
GetPriceListService
```

### Function adapter

Usar:

```text
<Action><Resource>Function
<Action><Resource>Request
<Action><Resource>Response
```

No usar sufijo `DTO` salvo que no haya un nombre mejor.

Ejemplo:

```text
GetPriceListFunction
GetPriceListRequest
GetPriceListResponse
```

La function es un adapter técnico. No debe saber de JSON:API ni de API Gateway.

### API / JSON:API adapter

Usar:

```text
<Action><Resource>RouteHandler
<Resource>JsonApiMapper
<Resource>Attributes
<Resource><NestedItem>Attributes
```

Ejemplo:

```text
GetPriceListRouteHandler
PriceListJsonApiMapper
PriceListAttributes
PriceListProductAttributes
```

El route handler entiende API Gateway. El mapper entiende JSON:API. Ninguna de esas responsabilidades entra al use case.

## Requests GET vs POST/PATCH

Para un GET con id por path:

```text
GET /price-lists/{priceListId}
```

No hace falta body JSON:API. El id se toma desde `pathParameters`.

Para POST/PATCH, el body sí debería entrar como JSON:API:

```json
{
  "data": {
    "type": "price-lists",
    "attributes": {
      "name": "Lista verano",
      "currency": "BRL"
    }
  }
}
```

La conversión futura sería:

```text
JsonApiDocument<CreatePriceListAttributes>
  -> PriceListJsonApiMapper.toCreateRequest(...)
  -> CreatePriceListRequest
  -> CreatePriceListFunction
  -> CreatePriceListUseCase
```

## Manejo de errores

Las excepciones se centralizan en:

```text
ApiExceptionHandler
```

Mapeo base:

```text
400 Bad Request
  request mal formado, path param faltante, query param inválido, JSON inválido

404 Not Found
  recurso inexistente

409 Conflict
  conflicto de estado, duplicado, concurrencia

422 Unprocessable Entity
  request válido a nivel sintáctico, pero viola una regla de negocio

429 Too Many Requests
  rate limit o cuota excedida

500 Internal Server Error
  error inesperado
```

Las respuestas de error salen como JSON:API:

```json
{
  "errors": [
    {
      "status": "422",
      "title": "Unprocessable Entity",
      "detail": "Business rule violated"
    }
  ]
}
```

## Wiring Spring

Se usa wiring explícito con `@Configuration` + `@Bean`.

Motivo:

```text
Las clases quedan puras.
La infraestructura arma las dependencias.
La arquitectura hexagonal queda visible.
Es más fácil controlar qué adapter conecta con qué port/use case.
```

Configs actuales:

```text
ApiGatewayConfig
  JsonApiResponseFactory
  ApiExceptionHandler
  apiGatewayRouter

PriceListConfig
  GetPriceListUseCase
  GetPriceListFunction
  listPrice local/direct bean
  GetPriceListRouteHandler

HealthConfig
  health local/direct bean
  GetHealthRouteHandler

PersistenceConfig
  LoadProductsPort

JacksonConfig
  ObjectMapper
```

## Checklist para agregar un endpoint

```text
1. Crear RouteHandler en infrastructure.adapter.in.api.<resource>
2. Crear JsonApiMapper y Attributes si hay request/response JSON:API
3. Crear Function en infrastructure.adapter.in.function.<resource>
4. Crear Request/Response de esa function
5. Crear UseCase + Command en application.port.in.<resource>
6. Crear Service en application.service.<resource>
7. Crear/usar ports out si hacen falta
8. Agregar beans en <Resource>Config
9. Agregar ruta en template.yaml / OpenAPI
10. Agregar evento local de API Gateway si querés probar el router con SAM
```

## Frase guía para generación con IA

```text
Create code following hexagonal architecture.
JSON:API serialization/deserialization must stay only in infrastructure.adapter.in.api.
Spring Cloud Function adapters must stay in infrastructure.adapter.in.function.
Use cases must not depend on AWS, API Gateway, JSON:API, Jackson, or Spring Cloud Function.
Use explicit @Bean wiring in infrastructure.config.
Do not use DTO suffix; use Request/Response for function contracts, Command for application input, and Attributes/Resource/Document for JSON:API contracts.
```
