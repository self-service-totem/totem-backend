package com.ffresco.pricelist.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JsonApiResponseFactory {

    private static final Map<String, String> JSON_API_HEADERS = Map.of(
            "Content-Type", "application/vnd.api+json"
    );

    private final ObjectMapper objectMapper;

    public JsonApiResponseFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public APIGatewayV2HTTPResponse ok(JsonApiResource<?> resource) {
        return json(200, Map.of("data", resource));
    }

    public APIGatewayV2HTTPResponse created(JsonApiResource<?> resource) {
        return json(201, Map.of("data", resource));
    }

    public APIGatewayV2HTTPResponse noContent() {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(204)
                .withHeaders(JSON_API_HEADERS)
                .withBody("")
                .build();
    }

    public APIGatewayV2HTTPResponse badRequest(String detail) {
        return error(400, "Bad Request", detail);
    }

    public APIGatewayV2HTTPResponse unprocessableEntity(String detail) {
        return error(422, "Unprocessable Entity", detail);
    }

    public APIGatewayV2HTTPResponse notFound(String detail) {
        return error(404, "Not Found", detail);
    }

    public APIGatewayV2HTTPResponse conflict(String detail) {
        return error(409, "Conflict", detail);
    }

    public APIGatewayV2HTTPResponse tooManyRequests(String detail) {
        return error(429, "Too Many Requests", detail);
    }

    public APIGatewayV2HTTPResponse internalServerError(String detail) {
        return error(500, "Internal Server Error", detail);
    }

    private APIGatewayV2HTTPResponse error(int statusCode, String title, String detail) {
        var error = new JsonApiError(String.valueOf(statusCode), title, detail);
        return json(statusCode, Map.of("errors", List.of(error)));
    }

    private APIGatewayV2HTTPResponse json(int statusCode, Object body) {
        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(JSON_API_HEADERS)
                    .withBody(objectMapper.writeValueAsString(body))
                    .build();
        } catch (JsonProcessingException e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withHeaders(JSON_API_HEADERS)
                    .withBody("{\"errors\":[{\"status\":\"500\",\"title\":\"Internal Server Error\",\"detail\":\"Error serializing response\"}]}")
                    .build();
        }
    }
}
