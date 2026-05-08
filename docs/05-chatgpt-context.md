# ChatGPT Context - Backend Function Template

Use this file as context when asking ChatGPT to create or modify backend functions in this project.

## Project style

- Java 21
- Spring Cloud Function
- AWS Lambda
- API Gateway HTTP API v2
- SAM CLI
- Hexagonal Architecture
- JSON:API responses for remote/API Gateway endpoints

## Package rules

- `domain` must not depend on Spring or AWS.
- `application` should ideally not depend on Spring or AWS.
- `infrastructure` can depend on Spring, AWS Lambda events, SAM, DynamoDB, etc.

## Ports

- `application.port.in`: use case interfaces and commands.
- `application.port.out`: output ports required by use cases.

## Runtime modes

### Local fast mode

Local fast mode uses Spring Cloud Function Web and simple request/response objects.

Current local function:

```text
listPrice
```

Call:

```bash
mvn -Plocal spring-boot:run
```

```bash
curl -X POST "http://localhost:8080/listPrice" \
  -H "Content-Type: application/json" \
  -d '{"priceListId":"default"}'
```

### AWS remote mode

AWS remote mode uses API Gateway HTTP API v2 and the shared router function:

```text
apiGatewayRouter
```

SAM sets:

```yaml
SPRING_CLOUD_FUNCTION_DEFINITION: apiGatewayRouter
SPRING_MAIN_WEB_APPLICATION_TYPE: none
```

Call remote API Gateway with Postman/curl.

## API Gateway router pattern

Do not create one full API Gateway adapter per endpoint.

Use one shared router:

```text
infrastructure.adapter.in.api.ApiGatewayRouterFunction
```

The router receives `APIGatewayV2HTTPEvent`, reads `event.getRouteKey()`, and dispatches to the matching `ApiGatewayRouteHandler`.

Each new endpoint creates only a small route handler:

```text
infrastructure.adapter.in.api.<module>.<UseCase>RouteHandler
```

Example current route handler:

```text
infrastructure.adapter.in.api.pricelist.GetPriceListRouteHandler
```

## JSON:API

API Gateway responses should use:

```text
Content-Type: application/vnd.api+json
```

Success responses should use top-level `data`.

Error responses should use top-level `errors`.

Use:

```text
JsonApiResponseFactory
JsonApiResource
JsonApiError
```

## SAM rules

- `template.yaml` must use Java 21.
- Handler must be:

```text
org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
```

- Add API Gateway routes under the function `Events` section:

```yaml
Events:
  SomeRoute:
    Type: HttpApi
    Properties:
      ApiId: !Ref PriceListHttpApi
      Path: /some-path/{id}
      Method: GET
      PayloadFormatVersion: '2.0'
```

## Adding a new route

1. Create application command/use case/service.
2. Create output port/adapter if needed.
3. Create local/direct function if local fast testing is useful.
4. Create route handler implementing `ApiGatewayRouteHandler`.
5. Create JSON:API resource/attributes mapper.
6. Register beans in `ApplicationConfig`.
7. Add route in `template.yaml`.
8. Deploy with SAM.
9. Test with Postman/curl against AWS API Gateway.

## Future generator idea

Later, create a generator project that receives module name, package name and route definitions, then outputs a ready-to-run Lambda/API Gateway/SAM project ZIP.
