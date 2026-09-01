package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApiGatewayRouterFunction implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final Map<String, ApiGatewayRouteHandler> routeHandlers;
    private final ApiExceptionHandler exceptionHandler;

    public ApiGatewayRouterFunction(
            List<ApiGatewayRouteHandler> routeHandlers,
            ApiExceptionHandler exceptionHandler
    ) {
        this.routeHandlers = routeHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(ApiGatewayRouteHandler::routeKey, Function.identity()));
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
        try {
            ApiGatewayRouteHandler handler = resolveHandler(event);

            if (handler == null) {
                return exceptionHandler.handle(new IllegalArgumentException(
                        "No route handler found for routeKey: " + safeRouteKey(event)
                ));
            }

            return handler.handle(event);
        } catch (Exception exception) {
            return exceptionHandler.handle(exception);
        }
    }

    private ApiGatewayRouteHandler resolveHandler(APIGatewayV2HTTPEvent event) {
        String routeKey = safeRouteKey(event);

        ApiGatewayRouteHandler exactHandler = routeHandlers.get(routeKey);
        if (exactHandler != null) {
            return exactHandler;
        }

        String requestMethod = methodFromRouteKey(routeKey);
        String requestPath = pathFrom(event, routeKey);

        if (requestMethod == null || requestPath == null) {
            return null;
        }

        for (ApiGatewayRouteHandler handler : routeHandlers.values()) {
            ApiGatewayRouteMatch match = ApiGatewayRouteMatcher.match(
                    handler.routeKey(),
                    requestMethod,
                    requestPath
            );

            if (match.matches()) {
                event.setRouteKey(handler.routeKey());
                event.setPathParameters(match.pathParameters());
                return handler;
            }
        }

        return null;
    }

    private String safeRouteKey(APIGatewayV2HTTPEvent event) {
        if (event == null || event.getRouteKey() == null) {
            return null;
        }
        return event.getRouteKey();
    }

    private String methodFromRouteKey(String routeKey) {
        if (routeKey == null || routeKey.isBlank()) {
            return null;
        }

        String[] parts = routeKey.trim().split("\\s+", 2);
        return parts.length == 2 ? parts[0] : null;
    }

    private String pathFrom(APIGatewayV2HTTPEvent event, String routeKey) {
        if (event != null && event.getRawPath() != null && !event.getRawPath().isBlank()) {
            return event.getRawPath();
        }

        if (routeKey == null || routeKey.isBlank()) {
            return null;
        }

        String[] parts = routeKey.trim().split("\\s+", 2);
        return parts.length == 2 ? parts[1] : null;
    }
}
