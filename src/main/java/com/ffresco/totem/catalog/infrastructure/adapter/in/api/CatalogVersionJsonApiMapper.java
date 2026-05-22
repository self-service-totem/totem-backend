package com.ffresco.totem.catalog.infrastructure.adapter.in.api;

import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.totem.catalog.infrastructure.adapter.in.function.GetCatalogVersionResponse;

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
