package com.ffresco.totem.common.infrastructure.adapter.in.api;

import java.util.HashMap;
import java.util.Map;

public final class ApiGatewayRouteMatcher {

    private ApiGatewayRouteMatcher() {
    }

    /**
     * Matches an API Gateway HTTP API v2 route key against a concrete HTTP request.
     *
     * Example:
     * routeKey: GET /price-lists/{priceListId}
     * method:   GET
     * path:     /price-lists/default
     * result:   priceListId=default
     */
    public static ApiGatewayRouteMatch match(String routeKey, String method, String path) {
        if (routeKey == null || method == null || path == null) {
            return ApiGatewayRouteMatch.noMatch();
        }

        String[] routeParts = routeKey.trim().split("\\s+", 2);
        if (routeParts.length != 2) {
            return ApiGatewayRouteMatch.noMatch();
        }

        String routeMethod = routeParts[0];
        String routePathTemplate = normalizePath(routeParts[1]);
        String requestPath = normalizePath(path);

        if (!routeMethod.equalsIgnoreCase(method)) {
            return ApiGatewayRouteMatch.noMatch();
        }

        String[] templateSegments = segments(routePathTemplate);
        String[] pathSegments = segments(requestPath);

        if (templateSegments.length != pathSegments.length) {
            return ApiGatewayRouteMatch.noMatch();
        }

        Map<String, String> pathParameters = new HashMap<>();

        for (int i = 0; i < templateSegments.length; i++) {
            String templateSegment = templateSegments[i];
            String pathSegment = pathSegments[i];

            if (isPathParameter(templateSegment)) {
                String parameterName = templateSegment.substring(1, templateSegment.length() - 1);
                pathParameters.put(parameterName, pathSegment);
                continue;
            }

            if (!templateSegment.equals(pathSegment)) {
                return ApiGatewayRouteMatch.noMatch();
            }
        }

        return ApiGatewayRouteMatch.match(pathParameters);
    }

    private static boolean isPathParameter(String segment) {
        return segment.startsWith("{") && segment.endsWith("}") && segment.length() > 2;
    }

    private static String normalizePath(String path) {
        String normalized = path == null || path.isBlank() ? "/" : path.trim();

        int queryStart = normalized.indexOf('?');
        if (queryStart >= 0) {
            normalized = normalized.substring(0, queryStart);
        }

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private static String[] segments(String path) {
        String normalized = normalizePath(path);
        if ("/".equals(normalized)) {
            return new String[0];
        }
        return normalized.substring(1).split("/");
    }
}
