package com.ffresco.totem.publicapi.menu.application.service;

import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuCommand;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicMenuPort;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicTablePort;
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

class GetPublicMenuUseCaseImplTest {

    @Test
    void shouldReturnAvailableMenuForTable() {
        LoadPublicTablePort tables = tablePublicId -> new PublicTable(
                "tenant-001", "branch-001", "tbl-001", tablePublicId
        );
        LoadPublicMenuPort menus = (tenantId, branchId) -> new PublicMenu(
                tenantId,
                branchId,
                Currency.BRL,
                List.of(new PublicMenuCategory("cat-burgers", "Burgers", List.of(
                        product("prd-001", "Cheeseburger", true),
                        product("prd-002", "Veggie", false)
                )))
        );
        var service = new GetPublicMenuUseCaseImpl(tables, menus);

        var view = service.execute(new GetPublicMenuCommand("tbl-public-001"));

        assertThat(view.tablePublicId()).isEqualTo("tbl-public-001");
        assertThat(view.tableId()).isEqualTo("tbl-001");
        assertThat(view.menu().branchId()).isEqualTo("branch-001");
        assertThat(view.menu().categories()).hasSize(1);
        assertThat(view.menu().categories().get(0).products())
                .extracting(PublicMenuProduct::id)
                .containsExactly("prd-001");
    }

    @Test
    void shouldRejectBlankTableId() {
        LoadPublicTablePort tables = tablePublicId -> {
            throw new AssertionError("should not be called");
        };
        LoadPublicMenuPort menus = (tenantId, branchId) -> {
            throw new AssertionError("should not be called");
        };
        var service = new GetPublicMenuUseCaseImpl(tables, menus);

        assertThatThrownBy(() -> service.execute(new GetPublicMenuCommand("  ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tableId is required");
    }

    @Test
    void shouldPropagatePublicTableNotFound() {
        LoadPublicTablePort tables = tablePublicId -> {
            throw new PublicTableNotFoundException(tablePublicId);
        };
        LoadPublicMenuPort menus = (tenantId, branchId) -> {
            throw new AssertionError("should not be called");
        };
        var service = new GetPublicMenuUseCaseImpl(tables, menus);

        assertThatThrownBy(() -> service.execute(new GetPublicMenuCommand("tbl-missing")))
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
}
