package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRequest;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailFunction;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailRequest;

public class GetPublicProductDetailRouteHandler implements ApiGatewayRouteHandler {

    public static final String ROUTE_KEY = "GET /public/menu/products/{productId}";

    private final GetPublicProductDetailFunction getPublicProductDetailFunction;
    private final JsonApiResponseFactory responseFactory;

    public GetPublicProductDetailRouteHandler(
            GetPublicProductDetailFunction getPublicProductDetailFunction,
            JsonApiResponseFactory responseFactory
    ) {
        this.getPublicProductDetailFunction = getPublicProductDetailFunction;
        this.responseFactory = responseFactory;
    }

    @Override
    public String routeKey() {
        return ROUTE_KEY;
    }

    @Override
    public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event) {
        String productId = ApiGatewayRequest.requiredPathParameter(event, "productId");
        String tablePublicId = ApiGatewayRequest.requiredQueryParameter(event, "tableId");
        var response = getPublicProductDetailFunction.apply(
                new GetPublicProductDetailRequest(tablePublicId, productId)
        );
        return responseFactory.ok(PublicProductJsonApiMapper.toResource(response));
    }
}
