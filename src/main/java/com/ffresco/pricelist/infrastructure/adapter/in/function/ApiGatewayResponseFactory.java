package com.ffresco.pricelist.infrastructure.adapter.in.function;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ApiGatewayResponseFactory {

    private static final Map<String, String> JSON_HEADERS = Map.of(
            "Content-Type", "application/json"
    );

    private final ObjectMapper objectMapper;

    public ApiGatewayResponseFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public APIGatewayV2HTTPResponse ok(Object body) {
        return json(200, body);
    }

    public APIGatewayV2HTTPResponse badRequest(String message) {
        return json(400, new ErrorResponse(message));
    }

    public APIGatewayV2HTTPResponse internalServerError(String message) {
        return json(500, new ErrorResponse(message));
    }

    private APIGatewayV2HTTPResponse json(int statusCode, Object body) {
        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(JSON_HEADERS)
                    .withBody(objectMapper.writeValueAsString(body))
                    .build();

        } catch (JsonProcessingException e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withHeaders(JSON_HEADERS)
                    .withBody("{\"message\":\"Error serializing response\"}")
                    .build();
        }
    }

    private record ErrorResponse(String message) {
    }
}
