package com.ffresco.totem.catalog.infrastructure.adapter.in.api;

import java.time.Instant;

public record CatalogVersionAttributes(
        String branchId,
        Instant catalogVersion,
        String priceListId
) {
}
