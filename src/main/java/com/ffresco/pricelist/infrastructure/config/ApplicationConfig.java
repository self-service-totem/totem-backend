package com.ffresco.pricelist.infrastructure.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.pricelist.application.port.in.GetPriceListUseCase;
import com.ffresco.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.pricelist.application.service.GetPriceListService;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouterFunction;
import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist.GetPriceListRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceFunction;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceRequest;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceResponse;
import com.ffresco.pricelist.infrastructure.adapter.out.memory.InMemoryProductAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
public class ApplicationConfig {

    @Bean
    public LoadProductsPort loadProductsPort() {
        return new InMemoryProductAdapter();
    }

    @Bean
    public GetPriceListUseCase getPriceListUseCase(LoadProductsPort loadProductsPort) {
        return new GetPriceListService(loadProductsPort);
    }

    @Bean
    public ListPriceFunction listPriceFunction(GetPriceListUseCase getPriceListUseCase) {
        return new ListPriceFunction(getPriceListUseCase);
    }

    /**
     * Local/direct function.
     *
     * Useful when running locally with spring-cloud-function-web:
     * POST /listPrice with a simple JSON body.
     */
    @Bean
    public Function<ListPriceRequest, ListPriceResponse> listPrice(ListPriceFunction listPriceFunction) {
        return listPriceFunction;
    }

    @Bean
    public JsonApiResponseFactory jsonApiResponseFactory(ObjectMapper objectMapper) {
        return new JsonApiResponseFactory(objectMapper);
    }

    @Bean
    public ApiGatewayRouteHandler getPriceListRouteHandler(
            ListPriceFunction listPriceFunction,
            JsonApiResponseFactory responseFactory
    ) {
        return new GetPriceListRouteHandler(listPriceFunction, responseFactory);
    }

    /**
     * AWS/API Gateway entrypoint.
     *
     * API Gateway sends APIGatewayV2HTTPEvent to this single router.
     * The router dispatches by routeKey, for example: GET /price-lists/{priceListId}.
     */
    @Bean
    public Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> apiGatewayRouter(
            List<ApiGatewayRouteHandler> routeHandlers,
            JsonApiResponseFactory responseFactory
    ) {
        return new ApiGatewayRouterFunction(routeHandlers, responseFactory);
    }
}
