# Changelog

All notable changes to this project are documented in this file.

## [1.0.2] - 2026-05-08

### Changed
- SAM template updated to use source-based packaging with `CodeUri: ./` for local build compatibility.
- Lambda runtime boot configuration standardized with `MAIN_CLASS: com.ffresco.pricelist.PriceListLambdaApplication`.
- API outputs refined to include base API URL and a concrete `GetPriceList` example URL.

### Fixed
- SAM transform/deploy error caused by invalid `DefinitionUri` usage in `AWS::Serverless::HttpApi`.
- Local route mounting issues for `GET /health` by keeping explicit HTTP API events in `template.yaml`.
- Local SAM validation flow now passes with the current template configuration (`sam validate`).

## [1.0.0] - 2026-05-08

### Added
- Health endpoint routed through API Gateway (`GET /health`).
- JSON:API response support for:
  - `422 Unprocessable Entity`
  - `429 Too Many Requests`
- Local Lambda/API Gateway event testing support for the health route.
- SAM template route configuration for:
  - `GET /price-lists/{priceListId}`
  - `GET /health`

### Changed
- Health function updated to a no-input style using `Supplier<String>` for local Spring Cloud Function compatibility.
- Spring function registration aligned so `health` can be resolved explicitly in local mode.
- API Gateway route handler integration adjusted for the health flow (`GET /health` route key dispatch).
- SAM local build/invoke flow aligned to source-based `CodeUri` and Spring main-class discovery.

### Fixed
- Local runtime error when invoking health with `Void` input type (`Can't have non-null input with Void input type`).
- Local SAM/API issues caused by incorrect health event wiring and route exposure.
- Template event structure issues that prevented `/health` from being mounted in `sam local start-api`.

### Notes
- AWS deployment uses:
  - `Handler`: `org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest`
  - `SPRING_CLOUD_FUNCTION_DEFINITION`: `apiGatewayRouter`

