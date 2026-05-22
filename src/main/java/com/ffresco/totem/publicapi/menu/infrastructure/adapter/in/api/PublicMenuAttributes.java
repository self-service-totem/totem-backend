package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import java.util.List;

public record PublicMenuAttributes(
        String branchId,
        String tableId,
        String currency,
        List<PublicMenuCategoryAttributes> categories
) {
}
