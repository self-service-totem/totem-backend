package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuUseCase;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuCategory;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuView;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuFunction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GetPublicMenuRouteHandlerTest {

    @Test
    void shouldReturnJsonApiPublicMenuResponse() {
        GetPublicMenuUseCase useCase = command -> new PublicMenuView(
                command.tablePublicId(),
                "tbl-001",
                new PublicMenu(
                        "tenant-001",
                        "branch-001",
                        Currency.BRL,
                        List.of(new PublicMenuCategory(
                                "cat-burgers",
                                "Burgers",
                                List.of(new PublicMenuProduct(
                                        "prd-001",
                                        "Cheeseburger",
                                        "Beef patty.",
                                        new Money(new BigDecimal("12.5"), Currency.BRL),
                                        true
                                ))
                        ))
                )
        );
        var responseFactory = new JsonApiResponseFactory(new ObjectMapper().findAndRegisterModules());
        var handler = new GetPublicMenuRouteHandler(new GetPublicMenuFunction(useCase), responseFactory);
        var event = new APIGatewayV2HTTPEvent();
        event.setQueryStringParameters(Map.of("tableId", "tbl-public-001"));

        var response = handler.handle(event);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/vnd.api+json");
        assertThat(response.getBody()).contains("\"type\":\"public-menu\"");
        assertThat(response.getBody()).contains("\"id\":\"branch-001-menu\"");
        assertThat(response.getBody()).contains("\"branchId\":\"branch-001\"");
        assertThat(response.getBody()).contains("\"tableId\":\"tbl-001\"");
        assertThat(response.getBody()).contains("\"currency\":\"BRL\"");
        assertThat(response.getBody()).contains("\"amount\":\"12.50\"");
    }
}
