package com.ffresco.totem.publicapi.menu.infrastructure.config;

import com.ffresco.totem.common.infrastructure.adapter.in.api.ApiGatewayRouteHandler;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiDocument;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuUseCase;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailUseCase;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicMenuPort;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicTablePort;
import com.ffresco.totem.publicapi.menu.application.service.GetPublicMenuUseCaseImpl;
import com.ffresco.totem.publicapi.menu.application.service.GetPublicProductDetailUseCaseImpl;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api.GetPublicMenuRouteHandler;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api.GetPublicProductDetailRouteHandler;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api.PublicMenuAttributes;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api.PublicMenuJsonApiMapper;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api.PublicProductAttributes;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api.PublicProductJsonApiMapper;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuFunction;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuRequest;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailFunction;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailRequest;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.out.dynamodb.DynamoDbPublicMenuAdapter;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.out.dynamodb.DynamoDbPublicTableAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

@Configuration
public class PublicMenuConfig {

    @Bean
    public LoadPublicTablePort loadPublicTablePort(
            DynamoDbClient dynamoDbClient,
            @Qualifier("totemCoreTableName") String tableName
    ) {
        return new DynamoDbPublicTableAdapter(dynamoDbClient, tableName);
    }

    @Bean
    public LoadPublicMenuPort loadPublicMenuPort(
            DynamoDbClient dynamoDbClient,
            @Qualifier("totemCoreTableName") String tableName
    ) {
        return new DynamoDbPublicMenuAdapter(dynamoDbClient, tableName);
    }

    @Bean
    public GetPublicMenuUseCase getPublicMenuUseCase(
            LoadPublicTablePort loadPublicTablePort,
            LoadPublicMenuPort loadPublicMenuPort
    ) {
        return new GetPublicMenuUseCaseImpl(loadPublicTablePort, loadPublicMenuPort);
    }

    @Bean
    public GetPublicProductDetailUseCase getPublicProductDetailUseCase(
            LoadPublicTablePort loadPublicTablePort,
            LoadPublicMenuPort loadPublicMenuPort
    ) {
        return new GetPublicProductDetailUseCaseImpl(loadPublicTablePort, loadPublicMenuPort);
    }

    @Bean
    public GetPublicMenuFunction getPublicMenuFunction(GetPublicMenuUseCase getPublicMenuUseCase) {
        return new GetPublicMenuFunction(getPublicMenuUseCase);
    }

    @Bean
    public GetPublicProductDetailFunction getPublicProductDetailFunction(
            GetPublicProductDetailUseCase getPublicProductDetailUseCase
    ) {
        return new GetPublicProductDetailFunction(getPublicProductDetailUseCase);
    }

    @Bean
    public ApiGatewayRouteHandler getPublicMenuRouteHandler(
            GetPublicMenuFunction getPublicMenuFunction,
            JsonApiResponseFactory responseFactory
    ) {
        return new GetPublicMenuRouteHandler(getPublicMenuFunction, responseFactory);
    }

    @Bean
    public ApiGatewayRouteHandler getPublicProductDetailRouteHandler(
            GetPublicProductDetailFunction getPublicProductDetailFunction,
            JsonApiResponseFactory responseFactory
    ) {
        return new GetPublicProductDetailRouteHandler(getPublicProductDetailFunction, responseFactory);
    }

    /**
     * Local/direct function for spring-cloud-function-web (POST /publicMenu).
     *
     * <p>Returns a {@link JsonApiDocument} so the local response shape matches
     * the Lambda response shape — both transports MUST use the same JSON:API
     * envelope.</p>
     */
    @Bean("publicMenu")
    public Function<GetPublicMenuRequest, JsonApiDocument<PublicMenuAttributes>> publicMenu(
            GetPublicMenuFunction getPublicMenuFunction
    ) {
        return request -> new JsonApiDocument<>(
                PublicMenuJsonApiMapper.toResource(getPublicMenuFunction.apply(request))
        );
    }

    /**
     * Local/direct function for spring-cloud-function-web (POST /publicProduct).
     *
     * <p>Returns a {@link JsonApiDocument} so the local response shape matches
     * the Lambda response shape — both transports MUST use the same JSON:API
     * envelope.</p>
     */
    @Bean("publicProduct")
    public Function<GetPublicProductDetailRequest, JsonApiDocument<PublicProductAttributes>> publicProduct(
            GetPublicProductDetailFunction getPublicProductDetailFunction
    ) {
        return request -> new JsonApiDocument<>(
                PublicProductJsonApiMapper.toResource(getPublicProductDetailFunction.apply(request))
        );
    }
}
