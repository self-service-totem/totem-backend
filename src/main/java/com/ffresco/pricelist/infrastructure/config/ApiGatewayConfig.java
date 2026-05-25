package com.ffresco.pricelist.infrastructure.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiExceptionHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouterFunction;
import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResponseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public JsonApiResponseFactory jsonApiResponseFactory(ObjectMapper objectMapper) {
        return new JsonApiResponseFactory(objectMapper);
    }

    @Bean
    public ApiExceptionHandler apiExceptionHandler(JsonApiResponseFactory responseFactory) {
        return new ApiExceptionHandler(responseFactory);
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
            ApiExceptionHandler exceptionHandler
    ) {
        return new ApiGatewayRouterFunction(routeHandlers, exceptionHandler);
    }
}
