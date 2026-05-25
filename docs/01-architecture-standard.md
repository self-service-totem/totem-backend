# Backend Architecture Standard

## Objetivo

Estandarizar la construcción del backend Totem SaaS con Java 21, Spring Cloud Function, AWS Lambda, API Gateway HTTP API v2, SAM, DynamoDB, JSON:API y arquitectura hexagonal.

## Regla central

```text
Infrastructure adapts. Application orchestrates. Domain decides.
```

Dirección de dependencias:

```text
infrastructure -> application -> domain
```

`domain` nunca conoce AWS, API Gateway, Lambda, Jackson, JSON:API, Spring Cloud Function, DynamoDB ni frameworks externos.

## Runtime AWS

```text
API Gateway HTTP API v2
  -> Lambda Java 21
  -> Spring Cloud Function FunctionInvoker
  -> apiGatewayRouter
  -> ApiGatewayRouterFunction
  -> ApiGatewayRouteHandler
  -> JsonApiMapper
  -> Function adapter
  -> UseCase
  -> Domain / ports
  -> Adapter out
```

La variable obligatoria en AWS/SAM es:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

## Runtime local directo

El modo local directo sirve para probar una función Spring Cloud Function sin simular API Gateway.

```text
POST /<functionName>
  -> Function adapter
  -> UseCase
```

No se debe commitear un cambio accidental de `spring.cloud.function.definition` usado solo para pruebas locales.

## Package standard

Base package:

```text
com.ffresco.totem
```

Organización por capability:

```text
com.ffresco.totem.<capability>
  domain
    model
    enums
    exception
    service
  application
    port
      in
      out
    service
  infrastructure
    adapter
      in
        api
        function
      out
        dynamodb
        memory
        external
    config
```

Ejemplos de capabilities:

```text
publicapi.menu
ordering
catalog
kitchen
payment
restaurant
notification
common
```

## Responsabilidades por capa

| Tipo | Responsabilidad | No debe conocer |
|---|---|---|
| `RouteHandler` | API Gateway route, path/query/body extraction, HTTP status | Reglas de negocio profundas, DynamoDB directo |
| `JsonApiMapper` | Mapear request/response HTTP a JSON:API | AWS deploy, persistencia |
| `Function` | Adapter Spring Cloud Function request/response | API Gateway event shape |
| `UseCase` | Orquestar una acción de aplicación | JSON:API, API Gateway, Lambda, DynamoDB SDK |
| `Domain Entity / Value Object` | Estado, invariantes y comportamiento de negocio | Frameworks, adapters, JSON, AWS |
| `Domain Service` | Reglas de negocio que coordinan objetos de dominio | Puertos externos, infraestructura |
| `Port out` | Necesidad externa expresada por aplicación | Implementación concreta |
| `Adapter out` | Implementación DynamoDB/memory/external | HTTP/API Gateway |
| `Config` | Wiring explícito con `@Bean` | Lógica de negocio |

## Naming standard

No usar sufijo `DTO` por defecto.

```text
Use case:             <Action><Resource>UseCase
Command/query:        <Action><Resource>Command | <Action><Resource>Query
Application service:  <Action><Resource>Service
Route handler:        <Action><Resource>RouteHandler
Function adapter:     <Action><Resource>Function
Function request:     <Action><Resource>Request
Function response:    <Action><Resource>Response
Function mapper:      <Action><Resource>FunctionMapper
JSON:API mapper:      <Resource>JsonApiMapper
Attributes:           <Resource>Attributes
DynamoDB adapter:     DynamoDb<Resource>Adapter
DynamoDB mapper:      DynamoDb<Resource>Mapper
```

## Mapper ownership rule

Las conversiones viven en mappers explícitos del adapter que realiza la conversión.

No poner métodos como `from`, `toDomain`, `toResponse`, `toEntity`, `toResource` dentro de:

- domain objects
- request/response records
- persistence item records
- JSON:API attribute records

Ubicación:

```text
API/JSON:API mapping      -> infrastructure.adapter.in.api.<resource>
Function mapping          -> infrastructure.adapter.in.function.<resource>
DynamoDB mapping          -> infrastructure.adapter.out.dynamodb.<resource>
External provider mapping -> infrastructure.adapter.out.external.<provider>
```

## Endpoint creation flow

Para cada endpoint nuevo:

```text
1. Definir comportamiento en ticket + Gherkin.
2. Actualizar OpenAPI fragments.
3. Crear RouteHandler.
4. Crear JsonApiMapper + Attributes.
5. Crear Function adapter + Request/Response + FunctionMapper.
6. Crear input port.
7. Crear use case/application service.
8. Crear domain model/service si hay regla de negocio.
9. Crear output port si necesita persistencia o sistemas externos.
10. Crear adapter out si aplica.
11. Registrar beans en config explícita.
12. Crear tests unitarios, acceptance y contract/smoke según corresponda.
```

## Reglas duras

- Una PR debe implementar una feature vertical concreta.
- No mezclar refactors globales con feature PRs.
- No introducir frameworks nuevos sin decisión explícita.
- No modificar archivos no relacionados.
- No agregar rutas directamente en `template.yaml` mediante `HttpApi Events` si el contrato viene de OpenAPI.
- No poner lógica de negocio en route handlers, mappers, adapters, configs o repositorios.
- Toda lógica relevante debe ser testeable sin AWS, Spring ni DynamoDB.
