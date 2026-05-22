Necesito que me armes el prompt para Claude Code usando mi comando /create-pr.

Feature / PR:
[Nombre del PR o feature]

Épica:
[Nombre o número de épica]

Objetivo de negocio:
[Qué problema resuelve y para quién]

Endpoints a implementar:
- [METHOD] [PATH]
- [METHOD] [PATH]

Capability/package esperado:
com.ffresco.totem.[capability]

Casos funcionales:
1. Caso exitoso:
   [describir qué debe pasar]

2. Caso de error:
   [describir error principal]

3. Caso de borde:
   [describir borde: vacío, límite, inexistente, estado inválido, etc.]

Necesito que el prompt final incluya:

- Descripción clara del objetivo.
- Endpoints.
- Ejemplo de request.
- Ejemplo de response JSON:API exitoso.
- Ejemplo de response JSON:API de error.
- Ejemplo de datos esperados en DynamoDB:
  - pk
  - sk
  - entityType
  - gsi1pk / gsi1sk si aplica
  - gsi2pk / gsi2sk si aplica
  - atributos principales
- Access pattern de DynamoDB.
- Use cases esperados.
- Input ports y output ports esperados.
- Adapters esperados.
- Tests unitarios esperados.
- Tests acceptance-style esperados.
- Qué NO debe hacer Claude.
- Qué debe inspeccionar antes de editar.
- Qué comandos debe correr después.

Usá como base las reglas que venimos definiendo:
- arquitectura hexagonal
- paquetes por capability
- OpenAPI separado y generado
- JSON:API
- DynamoDB single-table multi-tenant
- tests: pasa / no pasa / condición de borde
- acceptance-style en JUnit
- no tocar archivos no relacionados

Dame el prompt final listo para pegar después de /create-pr en Claude Code.