package com.ffresco.pricelist.infrastructure.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffresco.pricelist.application.port.in.GetPriceListUseCase;
import com.ffresco.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.pricelist.application.service.GetPriceListService;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ApiGatewayResponseFactory;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceApiGatewayAdapter;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceFunction;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceRequest;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceResponse;
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
    public ListPriceFunction listPriceFunction(GetPriceListUseCase getPriceListUseCase) {
        return new ListPriceFunction(getPriceListUseCase);
    }

    /**
     * Local/direct function.
     *
     * This function is useful when running the project locally with spring-cloud-function-web:
     * POST /listPrice with a simple JSON body.
     */
    @Bean
    public Function<ListPriceRequest, ListPriceResponse> listPrice(ListPriceFunction listPriceFunction) {
        return listPriceFunction;
    }

    @Bean
    public ApiGatewayResponseFactory apiGatewayResponseFactory(ObjectMapper objectMapper) {
        return new ApiGatewayResponseFactory(objectMapper);
    }

    /**
     * AWS/API Gateway function.
     *
     * This function receives the APIGatewayV2HTTPEvent sent by API Gateway HTTP API v2
     * and adapts it to the local/direct listPrice function.
     */
    @Bean
    public Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> listPriceApiGateway(
            ListPriceFunction listPriceFunction,
            ApiGatewayResponseFactory responseFactory
    ) {
        return new ListPriceApiGatewayAdapter(listPriceFunction, responseFactory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
