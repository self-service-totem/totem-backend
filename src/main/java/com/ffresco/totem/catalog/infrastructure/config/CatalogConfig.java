package com.ffresco.totem.catalog.infrastructure.config;

import com.ffresco.totem.catalog.application.port.in.GetCatalogVersionUseCase;
import com.ffresco.totem.catalog.application.port.out.LoadCatalogVersionPort;
import com.ffresco.totem.catalog.application.service.GetCatalogVersionUseCaseImpl;
import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiDocument;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.catalog.infrastructure.adapter.in.api.CatalogVersionAttributes;
import com.ffresco.totem.catalog.infrastructure.adapter.in.api.CatalogVersionJsonApiMapper;
import com.ffresco.totem.catalog.infrastructure.adapter.in.api.GetCatalogVersionRouteHandler;
import com.ffresco.totem.catalog.infrastructure.adapter.in.function.GetCatalogVersionFunction;
import com.ffresco.totem.catalog.infrastructure.adapter.in.function.GetCatalogVersionRequest;
import com.ffresco.totem.catalog.infrastructure.adapter.out.dynamodb.DynamoDbCatalogVersionAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

@Configuration
public class CatalogConfig {

    @Bean
    public LoadCatalogVersionPort getLoadCatalogVersionPort(
            DynamoDbClient dynamoDbClient,
            @Qualifier("totemCoreTableName") String tableName
    ) {
        return new DynamoDbCatalogVersionAdapter(dynamoDbClient, tableName);
    }


    @Bean
    public GetCatalogVersionUseCase getCatalogVersionUseCase(LoadCatalogVersionPort loadCatalogVersionPort) {
        return new GetCatalogVersionUseCaseImpl(loadCatalogVersionPort);
    }

    @Bean
    public GetCatalogVersionFunction getCatalogVersionFunction(GetCatalogVersionUseCase getCatalogVersionUseCase) {
        return new GetCatalogVersionFunction(getCatalogVersionUseCase);
    }

    @Bean
    public ApiGatewayRouteHandler getCatalogVersionRouteHandler(
            GetCatalogVersionFunction getCatalogVersionFunction,
            JsonApiResponseFactory responseFactory
    ) {
        return new GetCatalogVersionRouteHandler(getCatalogVersionFunction, responseFactory);
    }

    /**
     * Local/direct function for spring-cloud-function-web (POST /catalogVersion).
     *
     * <p>Returns a {@link JsonApiDocument} so the local response shape matches
     * the Lambda response shape — both transports MUST use the same JSON:API
     * envelope.</p>
     */
    @Bean("catalogVersion")
    public Function<GetCatalogVersionRequest, JsonApiDocument<CatalogVersionAttributes>> catalogVersion(
            GetCatalogVersionFunction getCatalogVersionFunction
    ) {
        return request -> new JsonApiDocument<>(
                CatalogVersionJsonApiMapper.toResource(getCatalogVersionFunction.apply(request))
        );
    }
}
