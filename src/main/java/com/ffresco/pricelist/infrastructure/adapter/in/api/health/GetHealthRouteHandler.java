package com.ffresco.pricelist.infrastructure.adapter.in.api.health;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResponseFactory;

import java.util.function.Supplier;

public class GetHealthRouteHandler implements ApiGatewayRouteHandler {

    public static final String ROUTE_KEY = "GET /health";

    private final Supplier<String> healthFunction;
    private final JsonApiResponseFactory responseFactory;

    public GetHealthRouteHandler(
            Supplier<String> healthFunction,
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
