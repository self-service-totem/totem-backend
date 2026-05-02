package com.ffresco.pricelist.infrastructure.adapter.in.function;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.Map;
import java.util.function.Function;

public class ListPriceApiGatewayAdapter implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ListPriceFunction listPriceFunction;
    private final ApiGatewayResponseFactory responseFactory;

    public ListPriceApiGatewayAdapter(
            ListPriceFunction listPriceFunction,
            ApiGatewayResponseFactory responseFactory
    ) {
        this.listPriceFunction = listPriceFunction;
        this.responseFactory = responseFactory;
    }

    @Override
    public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
        try {
            String priceListId = extractPriceListId(event);

            var request = new ListPriceRequest(priceListId);
            var response = listPriceFunction.apply(request);

            return responseFactory.ok(response);

        } catch (IllegalArgumentException e) {
            return responseFactory.badRequest(e.getMessage());

        } catch (Exception e) {
            return responseFactory.internalServerError("Internal server error");
        }
    }

    private String extractPriceListId(APIGatewayV2HTTPEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Request event is required");
        }

        Map<String, String> pathParameters = event.getPathParameters();

        if (pathParameters == null || !pathParameters.containsKey("priceListId")) {
            throw new IllegalArgumentException("Path parameter 'priceListId' is required");
        }

        String priceListId = pathParameters.get("priceListId");

        if (priceListId == null || priceListId.isBlank()) {
            throw new IllegalArgumentException("Path parameter 'priceListId' cannot be empty");
        }

        return priceListId;
    }
}
