package com.ffresco.totem.catalog.infrastructure.adapter.in.function;

import java.time.Instant;

public record GetCatalogVersionResponse(
        String branchId,
        Instant catalogVersion,
        String priceListId
) {
}
