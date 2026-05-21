package com.ffresco.pricelist.infrastructure.adapter.in.function.catalog;

import java.time.Instant;

public record GetCatalogVersionResponse(
        String branchId,
        Instant catalogVersion,
        String priceListId
) {
}
