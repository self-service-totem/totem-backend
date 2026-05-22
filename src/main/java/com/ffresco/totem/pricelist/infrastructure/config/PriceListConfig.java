package com.ffresco.totem.pricelist.infrastructure.config;

import com.ffresco.totem.pricelist.application.port.in.GetPriceListUseCase;
import com.ffresco.totem.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.totem.pricelist.application.service.GetPriceListService;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.api.GetPriceListRouteHandler;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListFunction;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListRequest;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListResponse;
import com.ffresco.totem.pricelist.infrastructure.adapter.out.memory.InMemoryProductAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class PriceListConfig {

    @Bean
    public LoadProductsPort loadProductsPort() {
        return new InMemoryProductAdapter();
    }

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
