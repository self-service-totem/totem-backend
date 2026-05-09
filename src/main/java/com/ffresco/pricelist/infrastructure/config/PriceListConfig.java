package com.ffresco.pricelist.infrastructure.config;

import com.ffresco.pricelist.application.port.in.pricelist.GetPriceListUseCase;
import com.ffresco.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.pricelist.application.service.pricelist.GetPriceListService;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist.GetPriceListRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist.GetPriceListFunction;
import com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist.GetPriceListRequest;
import com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist.GetPriceListResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class PriceListConfig {

    @Bean
    public GetPriceListUseCase getPriceListUseCase(LoadProductsPort loadProductsPort) {
        return new GetPriceListService(loadProductsPort);
    }

    @Bean
    public GetPriceListFunction getPriceListFunction(GetPriceListUseCase getPriceListUseCase) {
        return new GetPriceListFunction(getPriceListUseCase);
    }

    @Bean
    public ApiGatewayRouteHandler getPriceListRouteHandler(
            GetPriceListFunction getPriceListFunction,
            JsonApiResponseFactory responseFactory
    ) {
        return new GetPriceListRouteHandler(getPriceListFunction, responseFactory);
    }
    /**
     * Local/direct function.
     *
     * Useful when running locally with spring-cloud-function-web:
     * POST /listPrice with a simple JSON body.
     *
     * This bean name is intentionally kept as listPrice for local compatibility.
     */
    @Bean("listPrice")
    public Function<GetPriceListRequest, GetPriceListResponse> listPrice(GetPriceListFunction getPriceListFunction) {
        return getPriceListFunction;
    }


}
