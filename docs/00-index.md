# Totem Backend Documentation Framework

Este directorio funciona como documentación del proyecto y como framework de trabajo para humanos e IA.

La regla principal es: **cada decisión debe vivir en un solo lugar**. Los prompts, comandos y tickets no deben repetir arquitectura, testing ni modelo de datos; solo deben referenciarlos.

## Source of truth

| Tema | Documento canónico | Qué contiene |
|---|---|---|
| Arquitectura backend | `docs/01-architecture-standard.md` | Capas, paquetes, reglas hexagonales, runtime AWS/local, responsabilidades |
| Testing | `docs/02-testing-strategy.md` | Pirámide de tests, unit/use case/acceptance/integration, Gherkin como lenguaje común |
| Modelo de datos | `docs/03-data-model.md` | Modelo conceptual, DynamoDB single-table, access patterns, PK/SK/GSI, reglas de persistencia |
| API contract | `docs/04-api-contract-standard.md` | OpenAPI-first, JSON:API, errores, contenido de requests/responses |
| Operación local/AWS | `docs/05-local-aws-runbook.md` | Build, deploy, SAM, Dynamo local/remoto, troubleshooting básico |
| Tickets Jira / PRs | `docs/docs/tickets/pr-ticket-template.md` | Formato único de ticket, con Gherkin y Definition of Done |
| Prompt para crear ticket | `docs/docs/tickets/prompt-create-ticket.md` | Prompt para pedirme a mí que genere un ticket |
| Comando para implementar | `.claude/commands/create-pr.md` | Comando de Claude Code que implementa un ticket siguiendo los docs |
| Reglas de Cursor | `.cursor/rules/backend-standard.mdc` | Reglas mínimas para que Cursor aplique el estándar siempre |

## Regla de no duplicación

- Los tickets describen **qué comportamiento implementar**, no repiten arquitectura.
- Los comandos dicen **qué documentos leer**, no duplican todas las reglas.
- Las reglas de Cursor son un resumen operativo, no una segunda documentación.
- `CLAUDE.md` debe ser un índice corto, no un documento gigante.
- El modelo de datos no debe vivir dentro de tickets, salvo ejemplos mínimos del access pattern afectado.

## Flujo estándar de trabajo

```text
1. Definir o elegir una Épica / PR desde `docs/planning/masterplan_prs_api_v1.md`.
2. Crear ticket con `docs/tickets/pr-ticket-template.md`.
3. Expresar comportamiento en Gherkin.
4. Validar si requiere cambio de modelo de datos.
5. Validar si requiere cambio de OpenAPI.
6. Pasar el ticket a Claude Code con `/create-pr`.
7. Claude implementa una feature vertical siguiendo los documentos canónicos.
8. Revisar que tests, OpenAPI, JSON:API y DynamoDB respeten el estándar.
```

## Orden recomendado para una IA

Cuando una IA tenga que implementar una PR, debe leer en este orden:

```text
1. CLAUDE.md
2. docs/tickets/<ticket>.md
3. docs/01-architecture-standard.md
4. docs/02-testing-strategy.md
5. docs/03-data-model.md
6. docs/04-api-contract-standard.md
7. docs/05-local-aws-runbook.md, solo si necesita ejecutar/deployar
```
