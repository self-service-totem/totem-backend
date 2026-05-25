package com.ffresco.pricelist.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public interface ApiGatewayRouteHandler {

    /**
     * API Gateway HTTP API v2 route key.
     * Example: GET /price-lists/{priceListId}
     */
    String routeKey();

    APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event);
}
