package com.ffresco.pricelist.domain.model;

import java.time.Instant;
import java.util.Objects;

public record CatalogVersion(
        String branchId,
        Instant catalogVersion,
        String priceListId
) {

    public CatalogVersion {
        Objects.requireNonNull(branchId, "branchId is required");
        Objects.requireNonNull(catalogVersion, "catalogVersion is required");
        Objects.requireNonNull(priceListId, "priceListId is required");
    }
}
