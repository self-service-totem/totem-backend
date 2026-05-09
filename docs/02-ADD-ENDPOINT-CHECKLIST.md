# Add Endpoint Checklist

Use this every time a new path/method is added.

## Example

```text
POST /price-lists
```

## Steps

```text
1. OpenAPI
   - Add path, method, parameters, requestBody, responses and schemas in openapi.yaml.

2. SAM
   - Add/confirm the matching HttpApi event in template.yaml.
   - Keep Lambda function definition as apiGatewayRouter for AWS/SAM.

3. API adapter
   - Create infrastructure.adapter.in.api.<resource>.<Action><Resource>RouteHandler.
   - Extract path/query/header/body from APIGatewayV2HTTPEvent.
   - Use JsonApiMapper for input/output.

4. JSON:API mapper
   - Create/update <Resource>JsonApiMapper.
   - Create request/response Attributes records as needed.
   - GET by path id does not require JSON:API body.
   - POST/PATCH should parse JsonApiDocument<...Attributes>.

5. Function adapter
   - Create infrastructure.adapter.in.function.<resource>.<Action><Resource>Function.
   - Create <Action><Resource>Request.
   - Create <Action><Resource>Response.
   - Do not use DTO suffix by default.
   - Do not use JSON:API types here.

6. Application
   - Create application.port.in.<resource>.<Action><Resource>UseCase.
   - Create <Action><Resource>Command.
   - Create application.service.<resource>.<Action><Resource>Service.

7. Output ports/adapters
   - Add application.port.out only if the use case needs persistence, external APIs, queues or events.
   - Implement in infrastructure.adapter.out.memory/dynamodb/etc.

8. Config
   - Add beans in <Resource>Config.
   - Register use case, function and route handler.

9. Errors
   - Throw domain/application exceptions.
   - Map them centrally in ApiExceptionHandler.

10. Tests
   - Test direct function locally if useful.
   - Test API Gateway payload with SAM local or deployed API Gateway.
```

## Naming template

```text
<Action><Resource>RouteHandler
<Resource>JsonApiMapper
<Resource>Attributes
<Action><Resource>Function
<Action><Resource>Request
<Action><Resource>Response
<Action><Resource>UseCase
<Action><Resource>Command
<Action><Resource>Service
```

## Mnemonic

```text
O-S-A-F-A-C-T
OpenAPI -> SAM -> API adapter -> Function -> Application -> Config -> Test
```
