package com.ffresco.totem.publicapi.menu.domain.model;

import com.ffresco.totem.common.domain.enums.Currency;

import java.util.List;
import java.util.Objects;

public record PublicMenu(
        String tenantId,
        String branchId,
        Currency currency,
        List<PublicMenuCategory> categories
) {
    public PublicMenu {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(branchId, "branchId is required");
        Objects.requireNonNull(currency, "currency is required");
        categories = categories == null ? List.of() : List.copyOf(categories);
    }

    /**
     * Public-facing view of the menu: only available products are visible, and
     * categories that end up empty after filtering are dropped.
     */
    public PublicMenu availableOnly() {
        List<PublicMenuCategory> filtered = categories.stream()
                .map(category -> category.withProducts(
                        category.products().stream()
                                .filter(PublicMenuProduct::available)
                                .toList()
                ))
                .filter(category -> !category.products().isEmpty())
                .toList();
        return new PublicMenu(tenantId, branchId, currency, filtered);
    }
}
