package com.ffresco.totem.common.infrastructure.adapter.in.api;

import java.util.Map;

public record ApiGatewayRouteMatch(
        boolean matches,
        Map<String, String> pathParameters
) {
    public static ApiGatewayRouteMatch noMatch() {
        return new ApiGatewayRouteMatch(false, Map.of());
    }

    public static ApiGatewayRouteMatch match(Map<String, String> pathParameters) {
        return new ApiGatewayRouteMatch(true, Map.copyOf(pathParameters));
    }
}
