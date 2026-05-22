package com.ffresco.totem.catalog.infrastructure.adapter.in.function;

import com.ffresco.totem.catalog.application.port.in.GetCatalogVersionCommand;
import com.ffresco.totem.catalog.domain.model.CatalogVersion;

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
