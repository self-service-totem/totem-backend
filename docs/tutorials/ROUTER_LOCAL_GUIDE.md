# Router compartido local + AWS

## Objetivo

El frontend debe consumir los mismos paths en local y en AWS:

```http
GET /health
GET /price-lists/{priceListId}
GET /branches/{branchId}/catalog/version
GET /public/menu?tableId=...
GET /public/menu/products/{productId}?tableId=...
```

Sin adaptar el frontend a los endpoints internos `POST /functionName` de Spring Cloud Function.

## Cambio aplicado

Se mantiene el router existente `ApiGatewayRouterFunction` y se agrega una entrada local mínima:

```text
Frontend local
  -> GET http://localhost:8080/price-lists/default
  -> LocalApiController
  -> APIGatewayV2HTTPEvent simulado
  -> ApiGatewayRouterFunction
  -> RouteHandler existente
  -> Function / UseCase / Domain
```

En AWS no cambia el flujo:

```text
API Gateway
  -> Lambda
  -> ApiGatewayRouterFunction
  -> RouteHandler existente
  -> Function / UseCase / Domain
```

## Archivos agregados

```text
src/main/java/com/ffresco/totem/common/infrastructure/adapter/in/api/LocalApiController.java
src/main/java/com/ffresco/totem/common/infrastructure/adapter/in/api/ApiGatewayRouteMatcher.java
src/main/java/com/ffresco/totem/common/infrastructure/adapter/in/api/ApiGatewayRouteMatch.java
```

## Archivo modificado

```text
src/main/java/com/ffresco/totem/common/infrastructure/adapter/in/api/ApiGatewayRouterFunction.java
pom.xml
```

## Cómo correr local

```bash
mvn spring-boot:run -Plocal -Dspring-boot.run.profiles=local
```

Luego probar:

```bash
curl http://localhost:8080/health
curl http://localhost:8080/price-lists/default
curl 'http://localhost:8080/public/menu?tableId=table-public-001'
```

## Swagger UI local

Para probar endpoints desde el browser sin Postman/curl, con el server local corriendo:

```text
http://localhost:8080/docs
```

Sirve Swagger UI (vía CDN) apuntando al `openapi.yaml` ya bundleado (`http://localhost:8080/openapi.yaml`), y el botón "Try it out" ejecuta requests reales contra `LocalApiController`.

Solo existe bajo el profile `local` (`LocalSwaggerUiController`, `@Profile("local")`). Nunca se activa en Lambda.

## Qué actualizar cuando agregás un endpoint

### 1. OpenAPI

Agregar el path en `openapi-src/paths/...` y referenciarlo desde `openapi-src/openapi-root.yaml` si corresponde.

Ejemplo:

```yaml
/branches/{branchId}/catalog/version:
  get:
    operationId: getCatalogVersion
```

Después regenerar el bundle:

```bash
python3 scripts/bundle-openapi.py openapi-src/openapi-root.yaml openapi.yaml
```

O dejar que Maven lo haga en `generate-resources`.

### 2. Handler de ruta

Crear un handler que implemente `ApiGatewayRouteHandler`.

Ejemplo:

```java
public class GetSomethingRouteHandler implements ApiGatewayRouteHandler {

    public static final String ROUTE_KEY = "GET /something/{somethingId}";

    @Override
    public String routeKey() {
        return ROUTE_KEY;
    }

    @Override
    public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event) {
        String somethingId = ApiGatewayRequest.requiredPathParameter(event, "somethingId");
        // llamar function/use case
        // devolver JSON:API response
    }
}
```

### 3. Registrar el bean

Agregar el bean en el `Config` del módulo correspondiente.

Ejemplo:

```java
@Bean
public GetSomethingRouteHandler getSomethingRouteHandler(
        GetSomethingFunction function,
        JsonApiResponseFactory responseFactory
) {
    return new GetSomethingRouteHandler(function, responseFactory);
}
```

No necesitás tocar `ApiGatewayRouterFunction`: Spring inyecta automáticamente todos los beans que implementan `ApiGatewayRouteHandler`.

### 4. API Gateway / SAM / CloudFormation

Agregar el path y method en la definición de API Gateway, normalmente vía `openapi.yaml` con `x-amazon-apigateway-integration` apuntando a la misma Lambda router.

### 5. Test mínimo recomendado

Agregar test del handler:

```text
GetSomethingRouteHandlerTest
```

Y validar:

```text
- routeKey correcto
- path param requerido
- query param requerido si aplica
- response JSON:API esperado
```

### 6. Probar local con el path real

```bash
curl http://localhost:8080/something/123
```

El frontend local debería usar siempre paths reales, no `POST /functionName`.

## Regla mental

Cuando agregás un endpoint, tocás:

```text
1. OpenAPI
2. RouteHandler
3. Config del módulo para registrar el bean
4. SAM/API Gateway si corresponde
5. Test del handler
```

No tocás:

```text
- Frontend para cambiar método según local/AWS
- ApiGatewayRouterFunction para cada endpoint
- LocalApiController para cada endpoint
```
