# Testing Strategy

## Principio central

```text
No escribimos tests para subir cobertura.
Escribimos tests para proteger comportamiento de negocio.
```

Si una regla de negocio no se puede testear sin AWS, Spring, Lambda, API Gateway o DynamoDB real, probablemente está en la capa equivocada.

## Pirámide práctica

```text
70% domain + use case unit tests
20% acceptance-style business flow tests
10% integration / contract / smoke tests
```

Esto no es una métrica rígida. Es una guía para evitar muchos tests de glue code y pocos tests de negocio.

## Regla mínima por comportamiento

Cada regla de negocio importante debe tener:

```text
1. Pasa
2. No pasa
3. Condición de borde
```

En inglés para nombres de tests:

```text
happy path
invalid/rejected case
boundary case
```

## Domain unit tests

Protegen invariantes y reglas puras del dominio.

Deben usar objetos reales de dominio. No mockear entidades, value objects ni domain services.

Ejemplos:

```text
Order
OrderItem
TableSession
TableAccount
PaymentSplit
CatalogVersion
StockReservation
Branch
Tenant
Product
PriceList
Menu
KitchenOrder
Notification
```

## Use case tests

Protegen una acción completa de aplicación.

El use case puede orquestar:

```text
domain objects
ports out
validaciones de aplicación
transacciones lógicas
publicación de eventos
```

Los ports out se reemplazan con fakes o in-memory adapters, no con AWS real.

## Acceptance-style tests

Los escenarios Gherkin del ticket son la fuente de verdad para estos tests.

No usar Cucumber por ahora. El Gherkin sirve como lenguaje común para humanos, Jira e IA; la implementación se hace con JUnit.

Ubicación recomendada:

```text
src/test/java/acceptance/<capability>/
```

Estilo:

```java
@Test
void shouldReturnPublicMenuWhenTableQrCodeIsValid() {
    // Given
    // When
    // Then
}
```

Los acceptance tests deben usar:

```text
Use cases reales
Domain real
Fakes in-memory para ports out
Sin Spring context
Sin AWS
Sin DynamoDB real
```

## Integration / contract / smoke tests

Usarlos poco y en límites importantes:

```text
OpenAPI contract
JSON:API response shape
DynamoDB adapter mapping
API Gateway route wiring
SAM/AWS smoke endpoint
```

No deben reemplazar los tests de negocio.

## Qué NO testear en exceso

Evitar tests detallados para:

```text
constructores triviales
getters/setters/records
config beans sin lógica
mappers sin reglas complejas
framework wiring repetitivo
```

Solo testear mappers cuando haya transformación relevante o riesgo de contrato.

## Definition of test done por PR

Una PR está bien testeada cuando:

```text
- Cada escenario Gherkin tiene al menos un test acceptance o unitario equivalente.
- Cada regla de negocio tiene pasa / no pasa / borde.
- Los errores esperados tienen test.
- El contrato JSON:API relevante está cubierto.
- No se necesita AWS real para validar la lógica principal.
- Los tests corren con `mvn test`.
```
