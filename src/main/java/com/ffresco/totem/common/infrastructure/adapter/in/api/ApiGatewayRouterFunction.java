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
            String routeKey = ApiGatewayRequest.routeKey(event);
            ApiGatewayRouteHandler handler = routeHandlers.get(routeKey);

            if (handler == null) {
                return exceptionHandler.handle(new IllegalArgumentException(
                        "No route handler found for routeKey: " + routeKey
                ));
            }

            return handler.handle(event);
        } catch (Exception exception) {
            return exceptionHandler.handle(exception);
        }
    }
}
