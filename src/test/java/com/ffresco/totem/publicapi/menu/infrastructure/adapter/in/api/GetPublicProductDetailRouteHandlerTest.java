package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailUseCase;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import com.ffresco.totem.publicapi.menu.domain.model.PublicProductView;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailFunction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GetPublicProductDetailRouteHandlerTest {

    @Test
    void shouldReturnJsonApiPublicProductResponse() {
        GetPublicProductDetailUseCase useCase = command -> new PublicProductView(
                "tenant-001",
                "branch-001",
                "tbl-001",
                command.tablePublicId(),
                "cat-burgers",
                "Burgers",
                new PublicMenuProduct(
                        command.productId(),
                        "Cheeseburger",
                        "Beef patty.",
                        new Money(new BigDecimal("12.5"), Currency.BRL),
                        true
                )
        );
        var responseFactory = new JsonApiResponseFactory(new ObjectMapper().findAndRegisterModules());
        var handler = new GetPublicProductDetailRouteHandler(
                new GetPublicProductDetailFunction(useCase),
                responseFactory
        );
        var event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("productId", "prd-001"));
        event.setQueryStringParameters(Map.of("tableId", "tbl-public-001"));

        var response = handler.handle(event);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/vnd.api+json");
        assertThat(response.getBody()).contains("\"type\":\"public-product\"");
        assertThat(response.getBody()).contains("\"id\":\"prd-001\"");
        assertThat(response.getBody()).contains("\"branchId\":\"branch-001\"");
        assertThat(response.getBody()).contains("\"tableId\":\"tbl-001\"");
        assertThat(response.getBody()).contains("\"categoryId\":\"cat-burgers\"");
        assertThat(response.getBody()).contains("\"categoryName\":\"Burgers\"");
        assertThat(response.getBody()).contains("\"amount\":\"12.50\"");
        assertThat(response.getBody()).contains("\"currency\":\"BRL\"");
    }
}
