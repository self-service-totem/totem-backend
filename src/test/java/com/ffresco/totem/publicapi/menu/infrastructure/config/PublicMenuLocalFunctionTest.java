package com.ffresco.totem.publicapi.menu.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiDocument;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuUseCase;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailUseCase;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuCategory;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuView;
import com.ffresco.totem.publicapi.menu.domain.model.PublicProductView;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuFunction;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuRequest;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailFunction;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the local-direct beans exposed by {@link PublicMenuConfig}
 * (used by {@code mvn -Plocal spring-boot:run}) produce the same JSON:API
 * envelope as the Lambda/API Gateway path.
 */
class PublicMenuLocalFunctionTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final PublicMenuConfig config = new PublicMenuConfig();

    @Test
    void publicMenuLocalBeanReturnsJsonApiDocument() throws Exception {
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
                                        "Beef.",
                                        new Money(new BigDecimal("12.50"), Currency.BRL),
                                        true
                                ))
                        ))
                )
        );
        var localBean = config.publicMenu(new GetPublicMenuFunction(useCase));

        JsonApiDocument<?> document = localBean.apply(new GetPublicMenuRequest("tbl-public-001"));
        JsonNode tree = objectMapper.valueToTree(document);

        assertThat(tree.has("data")).isTrue();
        assertThat(tree.get("data").get("type").asText()).isEqualTo("public-menu");
        assertThat(tree.get("data").get("id").asText()).isEqualTo("branch-001-menu");
        var attributes = tree.get("data").get("attributes");
        assertThat(attributes.get("branchId").asText()).isEqualTo("branch-001");
        assertThat(attributes.get("tableId").asText()).isEqualTo("tbl-001");
        assertThat(attributes.get("currency").asText()).isEqualTo("BRL");
        assertThat(attributes.get("categories").get(0).get("id").asText()).isEqualTo("cat-burgers");
    }

    @Test
    void publicProductLocalBeanReturnsJsonApiDocument() throws Exception {
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
                        "Beef.",
                        new Money(new BigDecimal("12.50"), Currency.BRL),
                        true
                )
        );
        var localBean = config.publicProduct(new GetPublicProductDetailFunction(useCase));

        JsonApiDocument<?> document = localBean.apply(
                new GetPublicProductDetailRequest("tbl-public-001", "prd-001")
        );
        JsonNode tree = objectMapper.valueToTree(document);

        assertThat(tree.has("data")).isTrue();
        assertThat(tree.get("data").get("type").asText()).isEqualTo("public-product");
        assertThat(tree.get("data").get("id").asText()).isEqualTo("prd-001");
        var attributes = tree.get("data").get("attributes");
        assertThat(attributes.get("branchId").asText()).isEqualTo("branch-001");
        assertThat(attributes.get("tableId").asText()).isEqualTo("tbl-001");
        assertThat(attributes.get("name").asText()).isEqualTo("Cheeseburger");
        assertThat(attributes.get("categoryId").asText()).isEqualTo("cat-burgers");
        assertThat(attributes.get("categoryName").asText()).isEqualTo("Burgers");
        assertThat(attributes.get("price").get("amount").asText()).isEqualTo("12.50");
        assertThat(attributes.get("price").get("currency").asText()).isEqualTo("BRL");
    }
}
