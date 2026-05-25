package com.ffresco.totem.publicapi.menu.application.service;

import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailCommand;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicMenuPort;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicTablePort;
import com.ffresco.totem.publicapi.menu.domain.exception.PublicProductNotFoundException;
import com.ffresco.totem.publicapi.menu.domain.exception.PublicTableNotFoundException;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuCategory;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import com.ffresco.totem.publicapi.menu.domain.model.PublicTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetPublicProductDetailUseCaseImplTest {

    @Test
    void shouldReturnProductWithCategoryContext() {
        LoadPublicTablePort tables = tablePublicId -> new PublicTable(
                "tenant-001", "branch-001", "tbl-001", tablePublicId
        );
        LoadPublicMenuPort menus = (tenantId, branchId) -> new PublicMenu(
                tenantId,
                branchId,
                Currency.BRL,
                List.of(new PublicMenuCategory("cat-burgers", "Burgers", List.of(
                        product("prd-001", "Cheeseburger", true)
                )))
        );
        var service = new GetPublicProductDetailUseCaseImpl(tables, menus);

        var view = service.execute(new GetPublicProductDetailCommand("tbl-public-001", "prd-001"));

        assertThat(view.product().id()).isEqualTo("prd-001");
        assertThat(view.categoryId()).isEqualTo("cat-burgers");
        assertThat(view.categoryName()).isEqualTo("Burgers");
        assertThat(view.tenantId()).isEqualTo("tenant-001");
        assertThat(view.branchId()).isEqualTo("branch-001");
        assertThat(view.tableId()).isEqualTo("tbl-001");
    }

    @Test
    void shouldRejectBlankTableId() {
        var service = new GetPublicProductDetailUseCaseImpl(failingTables(), failingMenus());

        assertThatThrownBy(() -> service.execute(new GetPublicProductDetailCommand(" ", "prd-001")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tableId is required");
    }

    @Test
    void shouldRejectBlankProductId() {
        var service = new GetPublicProductDetailUseCaseImpl(failingTables(), failingMenus());

        assertThatThrownBy(() -> service.execute(new GetPublicProductDetailCommand("tbl-public-001", "  ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("productId is required");
    }

    @Test
    void shouldThrowWhenProductIsUnavailable() {
        LoadPublicTablePort tables = tablePublicId -> new PublicTable(
                "tenant-001", "branch-001", "tbl-001", tablePublicId
        );
        LoadPublicMenuPort menus = (tenantId, branchId) -> new PublicMenu(
                tenantId,
                branchId,
                Currency.BRL,
                List.of(new PublicMenuCategory("cat-burgers", "Burgers", List.of(
                        product("prd-002", "Veggie", false)
                )))
        );
        var service = new GetPublicProductDetailUseCaseImpl(tables, menus);

        assertThatThrownBy(() -> service.execute(new GetPublicProductDetailCommand("tbl-public-001", "prd-002")))
                .isInstanceOf(PublicProductNotFoundException.class);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        LoadPublicTablePort tables = tablePublicId -> new PublicTable(
                "tenant-001", "branch-001", "tbl-001", tablePublicId
        );
        LoadPublicMenuPort menus = (tenantId, branchId) -> new PublicMenu(
                tenantId,
                branchId,
                Currency.BRL,
                List.of(new PublicMenuCategory("cat-burgers", "Burgers", List.of(
                        product("prd-001", "Cheeseburger", true)
                )))
        );
        var service = new GetPublicProductDetailUseCaseImpl(tables, menus);

        assertThatThrownBy(() -> service.execute(new GetPublicProductDetailCommand("tbl-public-001", "prd-999")))
                .isInstanceOf(PublicProductNotFoundException.class);
    }

    @Test
    void shouldPropagateTableNotFound() {
        LoadPublicTablePort tables = tablePublicId -> {
            throw new PublicTableNotFoundException(tablePublicId);
        };
        var service = new GetPublicProductDetailUseCaseImpl(tables, failingMenus());

        assertThatThrownBy(() -> service.execute(new GetPublicProductDetailCommand("tbl-missing", "prd-001")))
                .isInstanceOf(PublicTableNotFoundException.class);
    }

    private static PublicMenuProduct product(String id, String name, boolean available) {
        return new PublicMenuProduct(
                id,
                name,
                null,
                new Money(new BigDecimal("10.00"), Currency.BRL),
                available
        );
    }

    private static LoadPublicTablePort failingTables() {
        return tablePublicId -> {
            throw new AssertionError("should not be called");
        };
    }

    private static LoadPublicMenuPort failingMenus() {
        return (tenantId, branchId) -> {
            throw new AssertionError("should not be called");
        };
    }
}
