package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;

import java.util.Map;

public final class ApiGatewayRequest {

    private ApiGatewayRequest() {
    }

    public static String requiredPathParameter(APIGatewayV2HTTPEvent event, String name) {
        if (event == null) {
            throw new IllegalArgumentException("Request event is required");
        }

        Map<String, String> pathParameters = event.getPathParameters();
        if (pathParameters == null || !pathParameters.containsKey(name)) {
            throw new IllegalArgumentException("Path parameter '" + name + "' is required");
        }

        String value = pathParameters.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Path parameter '" + name + "' cannot be empty");
        }

        return value;
    }

    public static String requiredQueryParameter(APIGatewayV2HTTPEvent event, String name) {
        if (event == null) {
            throw new IllegalArgumentException("Request event is required");
        }

        Map<String, String> queryParameters = event.getQueryStringParameters();
        if (queryParameters == null || !queryParameters.containsKey(name)) {
            throw new IllegalArgumentException("Query parameter '" + name + "' is required");
        }

        String value = queryParameters.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Query parameter '" + name + "' cannot be empty");
        }

        return value;
    }

    public static String routeKey(APIGatewayV2HTTPEvent event) {
        if (event == null || event.getRouteKey() == null || event.getRouteKey().isBlank()) {
            throw new IllegalArgumentException("API Gateway routeKey is required");
        }
        return event.getRouteKey();
    }
}
