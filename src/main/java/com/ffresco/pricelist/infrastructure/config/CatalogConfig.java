package com.ffresco.pricelist.infrastructure.config;

import com.ffresco.pricelist.application.port.in.catalog.GetCatalogVersionUseCase;
import com.ffresco.pricelist.application.port.out.LoadCatalogVersionPort;
import com.ffresco.pricelist.application.service.catalog.GetCatalogVersionService;
import com.ffresco.pricelist.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.pricelist.infrastructure.adapter.in.api.catalog.GetCatalogVersionRouteHandler;
import com.ffresco.pricelist.infrastructure.adapter.in.function.catalog.GetCatalogVersionFunction;
import com.ffresco.pricelist.infrastructure.adapter.in.function.catalog.GetCatalogVersionRequest;
import com.ffresco.pricelist.infrastructure.adapter.in.function.catalog.GetCatalogVersionResponse;
import com.ffresco.pricelist.infrastructure.adapter.out.dynamodb.DynamoDbCatalogVersionAdapter;
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
        return new GetCatalogVersionService(loadCatalogVersionPort);
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
     * Local/direct function.
     *
     * Useful when running locally with spring-cloud-function-web:
     * POST /catalogVersion with a simple JSON body.
     */
    @Bean("catalogVersion")
    public Function<GetCatalogVersionRequest, GetCatalogVersionResponse> catalogVersion(
            GetCatalogVersionFunction getCatalogVersionFunction
    ) {
        return getCatalogVersionFunction;
    }
}
