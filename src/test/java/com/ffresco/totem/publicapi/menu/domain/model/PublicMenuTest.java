package com.ffresco.totem.publicapi.menu.domain.model;

import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicMenuTest {

    @Test
    void shouldRequireTenantId() {
        assertThatThrownBy(() -> new PublicMenu(null, "branch-001", Currency.BRL, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("tenantId is required");
    }

    @Test
    void shouldRequireBranchId() {
        assertThatThrownBy(() -> new PublicMenu("tenant-001", null, Currency.BRL, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("branchId is required");
    }

    @Test
    void shouldDefaultToEmptyCategories() {
        var menu = new PublicMenu("tenant-001", "branch-001", Currency.BRL, null);

        assertThat(menu.categories()).isEmpty();
    }

    @Test
    void availableOnlyShouldDropUnavailableProducts() {
        var burgers = new PublicMenuCategory(
                "cat-burgers",
                "Burgers",
                List.of(
                        product("prd-001", "Cheeseburger", true),
                        product("prd-002", "Veggie", false)
                )
        );
        var menu = new PublicMenu("tenant-001", "branch-001", Currency.BRL, List.of(burgers));

        var filtered = menu.availableOnly();

        assertThat(filtered.categories()).hasSize(1);
        assertThat(filtered.categories().get(0).products())
                .extracting(PublicMenuProduct::id)
                .containsExactly("prd-001");
    }

    @Test
    void availableOnlyShouldDropEmptyCategories() {
        var allUnavailable = new PublicMenuCategory(
                "cat-burgers",
                "Burgers",
                List.of(product("prd-002", "Veggie", false))
        );
        var withAvailable = new PublicMenuCategory(
                "cat-drinks",
                "Drinks",
                List.of(product("prd-100", "Cola", true))
        );
        var menu = new PublicMenu(
                "tenant-001",
                "branch-001",
                Currency.BRL,
                List.of(allUnavailable, withAvailable)
        );

        var filtered = menu.availableOnly();

        assertThat(filtered.categories())
                .extracting(PublicMenuCategory::id)
                .containsExactly("cat-drinks");
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
