# Local / AWS Runbook

## Build and test

```bash
mvn clean test
mvn -Dtest=ClassName#method test
mvn clean package
```

## OpenAPI bundle

```bash
make openapi-bundle
```

El bundle debe correr antes de build/deploy si se modifican fragments OpenAPI.

## SAM build/deploy

```bash
make sam-build
sam deploy
```

Primera vez:

```bash
sam deploy --guided
```

## Local DynamoDB

```bash
make dynamo-up-local
make dynamo-create-table-local
make dynamo-seed-catalog-version-local
```

Variables esperadas:

```text
AWS_REGION=sa-east-1
DYNAMODB_ENDPOINT=http://localhost:8000
TOTEM_CORE_TABLE_NAME=totem-core-local
```

## AWS mode

En Lambda/SAM:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

## Local direct mode

Para probar una función específica con Spring Cloud Function Web:

```bash
mvn -Plocal spring-boot:run
```

El body es un JSON simple de la función, no un evento API Gateway.

## Troubleshooting básico

### El endpoint retorna 404 y no llega a Lambda

Revisar:

```text
- Path exacto, sin espacios al final.
- Stage correcto: /local, /dev, /prod.
- OpenAPI bundle actualizado.
- API deploy actualizado.
- Método HTTP correcto.
```

### Lambda no encuentra la función

Revisar:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

### Cambié OpenAPI pero API Gateway no refleja cambios

Revisar:

```text
- openapi-src actualizado.
- openapi.yaml regenerado.
- SAM build ejecutado.
- Deploy ejecutado.
```

### JSON:API no aparece en local

Revisar que el modo local devuelva `JsonApiDocument<Attributes>` y que el content type lo ponga el advice/local handler correspondiente.
