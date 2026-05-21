package com.ffresco.pricelist.infrastructure.adapter.in.api.catalog;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRequest;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.pricelist.infrastructure.adapter.in.function.catalog.GetCatalogVersionFunction;
import com.ffresco.pricelist.infrastructure.adapter.in.function.catalog.GetCatalogVersionRequest;

public class GetCatalogVersionRouteHandler implements ApiGatewayRouteHandler {

    public static final String ROUTE_KEY = "GET /branches/{branchId}/catalog/version";

    private final GetCatalogVersionFunction getCatalogVersionFunction;
    private final JsonApiResponseFactory responseFactory;

    public GetCatalogVersionRouteHandler(
            GetCatalogVersionFunction getCatalogVersionFunction,
            JsonApiResponseFactory responseFactory
    ) {
        this.getCatalogVersionFunction = getCatalogVersionFunction;
        this.responseFactory = responseFactory;
    }

    @Override
    public String routeKey() {
        return ROUTE_KEY;
    }

    @Override
    public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event) {
        String branchId = ApiGatewayRequest.requiredPathParameter(event, "branchId");
        var response = getCatalogVersionFunction.apply(new GetCatalogVersionRequest(branchId));
        return responseFactory.ok(CatalogVersionJsonApiMapper.toResource(response));
    }
}
