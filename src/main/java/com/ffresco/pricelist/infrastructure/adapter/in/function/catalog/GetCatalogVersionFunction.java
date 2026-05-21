package com.ffresco.pricelist.infrastructure.adapter.in.function.catalog;

import com.ffresco.pricelist.application.port.in.catalog.GetCatalogVersionUseCase;

import java.util.function.Function;

/**
 * Spring Cloud Function adapter for the get-catalog-version use case.
 *
 * This class is an infrastructure inbound adapter. It does not know HTTP,
 * API Gateway, JSON:API, or response status codes. It only translates the
 * function contract into the application use case contract.
 */
public class GetCatalogVersionFunction implements Function<GetCatalogVersionRequest, GetCatalogVersionResponse> {

    private final GetCatalogVersionUseCase getCatalogVersionUseCase;

    public GetCatalogVersionFunction(GetCatalogVersionUseCase getCatalogVersionUseCase) {
        this.getCatalogVersionUseCase = getCatalogVersionUseCase;
    }

    @Override
    public GetCatalogVersionResponse apply(GetCatalogVersionRequest request) {
        var command = CatalogVersionFunctionMapper.toCommand(request);
        var catalogVersion = getCatalogVersionUseCase.execute(command);
        return CatalogVersionFunctionMapper.toResponse(catalogVersion);
    }
}
