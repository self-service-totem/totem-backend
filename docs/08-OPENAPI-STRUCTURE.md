# OpenAPI Structure

`openapi.yaml` is the bundled contract used by AWS SAM in `template.yaml`.

Do not edit `openapi.yaml` manually unless it is an emergency.
Prefer editing the source fragments under `openapi-src/` and then regenerate the bundle.

## Source layout

```text
openapi-src/
  openapi-root.yaml
  paths/
    health.yaml
    pricelist.yaml
    catalog.yaml
  components/
    schemas/
      common.yaml
      pricelist.yaml
      catalog.yaml
    responses/
      common.yaml
```

## Generate the SAM-ready OpenAPI file

```bash
make openapi-bundle
```

or directly:

```bash
python3 scripts/bundle-openapi.py
```

## Rule

Each business capability owns its own OpenAPI fragment:

- `catalog` owns catalog endpoints and schemas.
- `pricelist` owns price list endpoints and schemas.
- `common` owns shared schemas, errors, health, and reusable responses.

This lets the project start as one Lambda with one API contract and later split capabilities into separate Lambdas/APIs with less refactoring.
