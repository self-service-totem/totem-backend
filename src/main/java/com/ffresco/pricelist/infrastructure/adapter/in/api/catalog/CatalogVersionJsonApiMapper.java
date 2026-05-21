package com.ffresco.pricelist.infrastructure.adapter.in.api.catalog;

import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.pricelist.infrastructure.adapter.in.function.catalog.GetCatalogVersionResponse;

public final class CatalogVersionJsonApiMapper {

    public static final String TYPE = "catalog-versions";

    private CatalogVersionJsonApiMapper() {
    }

    public static JsonApiResource<CatalogVersionAttributes> toResource(GetCatalogVersionResponse response) {
        return new JsonApiResource<>(
                TYPE,
                response.branchId(),
                new CatalogVersionAttributes(
                        response.branchId(),
                        response.catalogVersion(),
                        response.priceListId()
                )
        );
    }
}
