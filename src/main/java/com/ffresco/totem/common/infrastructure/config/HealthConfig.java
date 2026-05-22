package com.ffresco.totem.common.infrastructure.config;

import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.common.infrastructure.adapter.in.api.health.GetHealthRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.function.health.HealthFunction;
import com.ffresco.totem.common.infrastructure.adapter.in.function.health.HealthFunctionResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class HealthConfig {

    @Bean
    public ApiGatewayRouteHandler getHealthRouteHandler(
            Supplier<HealthFunctionResponse> healthFunction,
            JsonApiResponseFactory responseFactory
    ) {
        return new GetHealthRouteHandler(healthFunction, responseFactory);
    }

    /**
     * Local/direct function.
     *
     * This bean name is intentionally kept as health because application.yml
     * currently uses spring.cloud.function.definition=health for local testing.
     */
    @Bean("health")
    public Supplier<HealthFunctionResponse> health() {
        return new HealthFunction();
    }


}
