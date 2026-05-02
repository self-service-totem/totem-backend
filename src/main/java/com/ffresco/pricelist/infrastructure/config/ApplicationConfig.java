package com.ffresco.pricelist.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffresco.pricelist.application.port.in.GetPriceListUseCase;
import com.ffresco.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.pricelist.application.service.GetPriceListService;
import com.ffresco.pricelist.infrastructure.adapter.in.function.*;
import com.ffresco.pricelist.infrastructure.adapter.out.memory.InMemoryProductAdapter;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Function<ListPriceRequest, ListPriceResponse> listPrice(GetPriceListUseCase getPriceListUseCase) {
        return new ListPriceFunction(getPriceListUseCase);
    }

    @Bean
    public ApiGatewayResponseFactory apiGatewayResponseFactory(ObjectMapper objectMapper) {
        return new ApiGatewayResponseFactory(objectMapper);
    }

    @Bean
    public ListPriceFunction listPriceFunction(GetPriceListUseCase getPriceListUseCase) {
        return new ListPriceFunction(getPriceListUseCase);
    }

    @Bean
    public ListPriceApiGatewayAdapter listPrice(
            ListPriceFunction listPriceFunction,
            ApiGatewayResponseFactory responseFactory
    ) {
        return new ListPriceApiGatewayAdapter(listPriceFunction, responseFactory);
    }
}
