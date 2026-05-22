package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRequest;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuFunction;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuRequest;

public class GetPublicMenuRouteHandler implements ApiGatewayRouteHandler {

    public static final String ROUTE_KEY = "GET /public/menu";

    private final GetPublicMenuFunction getPublicMenuFunction;
    private final JsonApiResponseFactory responseFactory;

    public GetPublicMenuRouteHandler(
            GetPublicMenuFunction getPublicMenuFunction,
            JsonApiResponseFactory responseFactory
    ) {
        this.getPublicMenuFunction = getPublicMenuFunction;
        this.responseFactory = responseFactory;
    }

    @Override
    public String routeKey() {
        return ROUTE_KEY;
    }

    @Override
    public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event) {
        String tablePublicId = ApiGatewayRequest.requiredQueryParameter(event, "tableId");
        var response = getPublicMenuFunction.apply(new GetPublicMenuRequest(tablePublicId));
        return responseFactory.ok(PublicMenuJsonApiMapper.toResource(response));
    }
}
