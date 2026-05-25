# Prompt - Create Jira PR Ticket

Usá este prompt cuando quieras pedirme que cree un ticket Jira/PR listo para pegar y luego implementar con IA.

```text
Quiero crear un ticket Jira/PR para mi backend Totem SaaS.

Usá el formato de `docs/tickets/pr-ticket-template.md`.

No repitas reglas globales de arquitectura, testing, JSON:API, OpenAPI ni DynamoDB. Esas reglas viven en:

- `docs/01-architecture-standard.md`
- `docs/02-testing-strategy.md`
- `docs/03-data-model.md`
- `docs/04-api-contract-standard.md`

El ticket debe incluir sí o sí:

1. Description
2. Scope
3. Endpoints
4. Capability package
5. Expected use cases / ports / adapters
6. Out of scope
7. Functional scenarios en Gherkin
8. API contract
9. Data model impact, solo si aplica
10. Business rules
11. Test plan
12. Definition of Done

Contexto de la feature:
[PEGAR ACÁ LA IDEA DE LA FEATURE]

Épica:
[PEGAR ÉPICA]

PR esperada:
[PEGAR PR]

Endpoints tentativos:
[PEGAR ENDPOINTS SI LOS TENGO]

Reglas de negocio:
[PEGAR REGLAS]

Casos borde o errores esperados:
[PEGAR ERRORES]
```
