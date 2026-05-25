package com.ffresco.totem.pricelist.infrastructure.config;

import com.ffresco.totem.pricelist.application.port.in.GetPriceListUseCase;
import com.ffresco.totem.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.totem.pricelist.application.service.GetPriceListUseCaseImpl;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiDocument;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.api.GetPriceListRouteHandler;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.api.PriceListAttributes;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.api.PriceListJsonApiMapper;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListFunction;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListRequest;
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
        return new GetPriceListUseCaseImpl(loadProductsPort);
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
     * Local/direct function for spring-cloud-function-web (POST /listPrice).
     *
     * <p>Returns a {@link JsonApiDocument} so the local response shape matches
     * the Lambda response shape — both transports MUST use the same JSON:API
     * envelope.</p>
     */
    @Bean("listPrice")
    public Function<GetPriceListRequest, JsonApiDocument<PriceListAttributes>> listPrice(
            GetPriceListFunction getPriceListFunction
    ) {
        return request -> new JsonApiDocument<>(
                PriceListJsonApiMapper.toResource(getPriceListFunction.apply(request))
        );
    }
}
