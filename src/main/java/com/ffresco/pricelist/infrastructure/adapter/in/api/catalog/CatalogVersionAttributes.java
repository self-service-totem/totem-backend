package com.ffresco.pricelist.infrastructure.adapter.in.api.catalog;

import java.time.Instant;

public record CatalogVersionAttributes(
        String branchId,
        Instant catalogVersion,
        String priceListId
) {
}
