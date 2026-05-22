package com.ffresco.totem.common.infrastructure.adapter.in.api.health;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.common.infrastructure.adapter.in.function.health.HealthFunctionResponse;

import java.util.function.Supplier;

public class GetHealthRouteHandler implements ApiGatewayRouteHandler {

    public static final String ROUTE_KEY = "GET /health";

    private final Supplier<HealthFunctionResponse> healthFunction;
    private final JsonApiResponseFactory responseFactory;

    public GetHealthRouteHandler(
            Supplier<HealthFunctionResponse> healthFunction,
            JsonApiResponseFactory responseFactory
    ) {
        this.healthFunction = healthFunction;
        this.responseFactory = responseFactory;
    }

    @Override
    public String routeKey() {
        return ROUTE_KEY;
    }

    @Override
    public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event) {
        var response = healthFunction.get();
        return responseFactory.ok(HealthJsonApiMapper.toResource(response));
    }
}
