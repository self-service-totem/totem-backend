package com.ffresco.pricelist.infrastructure.adapter.in.function.catalog;

import com.ffresco.pricelist.application.port.in.catalog.GetCatalogVersionCommand;
import com.ffresco.pricelist.domain.model.CatalogVersion;

/**
 * Maps the Spring Cloud Function contract to/from application and domain objects.
 *
 * This mapper belongs to the inbound function adapter. The domain model does not
 * know request/response records and request/response records do not contain
 * domain conversion logic.
 */
final class CatalogVersionFunctionMapper {

    private CatalogVersionFunctionMapper() {
    }

    static GetCatalogVersionCommand toCommand(GetCatalogVersionRequest request) {
        return new GetCatalogVersionCommand(request == null ? null : request.branchId());
    }

    static GetCatalogVersionResponse toResponse(CatalogVersion catalogVersion) {
        return new GetCatalogVersionResponse(
                catalogVersion.branchId(),
                catalogVersion.catalogVersion(),
                catalogVersion.priceListId()
        );
    }
}
