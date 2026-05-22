package com.ffresco.totem.common.infrastructure.config;

import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiDocument;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.common.infrastructure.adapter.in.api.health.GetHealthRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.health.HealthAttributes;
import com.ffresco.totem.common.infrastructure.adapter.in.api.health.HealthJsonApiMapper;
import com.ffresco.totem.common.infrastructure.adapter.in.function.health.HealthFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class HealthConfig {

    @Bean
    public HealthFunction healthFunction() {
        return new HealthFunction();
    }

    @Bean
    public ApiGatewayRouteHandler getHealthRouteHandler(
            HealthFunction healthFunction,
            JsonApiResponseFactory responseFactory
    ) {
        return new GetHealthRouteHandler(healthFunction, responseFactory);
    }

    /**
     * Local/direct function for spring-cloud-function-web (POST /health).
     *
     * <p>Returns a {@link JsonApiDocument} so the local response shape matches
     * the Lambda response shape — both transports MUST use the same JSON:API
     * envelope.</p>
     *
     * <p>This bean name is intentionally kept as {@code health} because
     * {@code application.yml} uses {@code spring.cloud.function.definition=health}
     * for local testing.</p>
     */
    @Bean("health")
    public Supplier<JsonApiDocument<HealthAttributes>> health(HealthFunction healthFunction) {
        return () -> new JsonApiDocument<>(HealthJsonApiMapper.toResource(healthFunction.get()));
    }
}
