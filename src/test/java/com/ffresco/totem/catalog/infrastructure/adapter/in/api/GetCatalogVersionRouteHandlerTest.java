package com.ffresco.totem.catalog.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ffresco.totem.catalog.application.port.in.GetCatalogVersionCommand;
import com.ffresco.totem.catalog.application.port.in.GetCatalogVersionUseCase;
import com.ffresco.totem.catalog.domain.model.CatalogVersion;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResponseFactory;
import com.ffresco.totem.catalog.infrastructure.adapter.in.function.GetCatalogVersionFunction;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GetCatalogVersionRouteHandlerTest {

    @Test
    void shouldReturnJsonApiCatalogVersionResponse() {
        GetCatalogVersionUseCase useCase = (GetCatalogVersionCommand command) -> new CatalogVersion(
                command.branchId(),
                Instant.parse("2026-05-20T12:00:00Z"),
                "default"
        );
        var objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var responseFactory = new JsonApiResponseFactory(objectMapper);
        var handler = new GetCatalogVersionRouteHandler(
                new GetCatalogVersionFunction(useCase),
                responseFactory
        );
        var event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("branchId", "branch-001"));

        var response = handler.handle(event);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/vnd.api+json");
        assertThat(response.getBody()).contains("\"type\":\"catalog-versions\"");
        assertThat(response.getBody()).contains("\"id\":\"branch-001\"");
        assertThat(response.getBody()).contains("\"catalogVersion\":\"2026-05-20T12:00:00Z\"");
        assertThat(response.getBody()).contains("\"priceListId\":\"default\"");
    }
}
