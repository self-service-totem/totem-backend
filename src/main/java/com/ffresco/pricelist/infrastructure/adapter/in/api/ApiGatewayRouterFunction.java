package com.ffresco.pricelist.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApiGatewayRouterFunction implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final Map<String, ApiGatewayRouteHandler> routeHandlers;
    private final JsonApiResponseFactory responseFactory;

    public ApiGatewayRouterFunction(
            List<ApiGatewayRouteHandler> routeHandlers,
            JsonApiResponseFactory responseFactory
    ) {
        this.routeHandlers = routeHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(ApiGatewayRouteHandler::routeKey, Function.identity()));
        this.responseFactory = responseFactory;
    }

    @Override
    public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
        try {
            String routeKey = ApiGatewayRequest.routeKey(event);
            ApiGatewayRouteHandler handler = routeHandlers.get(routeKey);

            if (handler == null) {
                return responseFactory.notFound("No route handler found for routeKey: " + routeKey);
            }

            return handler.handle(event);
        } catch (IllegalArgumentException e) {
            return responseFactory.badRequest(e.getMessage());
        } catch (Exception e) {
            return responseFactory.internalServerError("Internal server error");
        }
    }
}
