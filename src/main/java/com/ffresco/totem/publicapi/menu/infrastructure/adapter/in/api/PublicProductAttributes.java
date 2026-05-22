package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

public record PublicProductAttributes(
        String branchId,
        String tableId,
        String name,
        String description,
        PublicMoneyAttributes price,
        boolean available,
        String categoryId,
        String categoryName
) {
}
