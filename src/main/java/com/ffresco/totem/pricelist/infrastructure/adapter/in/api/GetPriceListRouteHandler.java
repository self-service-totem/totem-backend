package com.ffresco.totem.pricelist.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRequest;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListFunction;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListRequest;

public class GetPriceListRouteHandler implements ApiGatewayRouteHandler {

    public static final String ROUTE_KEY = "GET /price-lists/{priceListId}";

    private final GetPriceListFunction getPriceListFunction;
    private final JsonApiResponseFactory responseFactory;

    public GetPriceListRouteHandler(
            GetPriceListFunction getPriceListFunction,
            JsonApiResponseFactory responseFactory
    ) {
        this.getPriceListFunction = getPriceListFunction;
        this.responseFactory = responseFactory;
    }

    @Override
    public String routeKey() {
        return ROUTE_KEY;
    }

    @Override
    public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event) {
        String priceListId = ApiGatewayRequest.requiredPathParameter(event, "priceListId");
        var response = getPriceListFunction.apply(new GetPriceListRequest(priceListId));
        return responseFactory.ok(PriceListJsonApiMapper.toResource(response));
    }
}
