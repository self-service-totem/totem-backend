package com.ffresco.pricelist.application.service.catalog;

import com.ffresco.pricelist.application.port.in.catalog.GetCatalogVersionCommand;
import com.ffresco.pricelist.application.port.in.catalog.GetCatalogVersionUseCase;
import com.ffresco.pricelist.application.port.out.LoadCatalogVersionPort;
import com.ffresco.pricelist.domain.model.CatalogVersion;

public class GetCatalogVersionService implements GetCatalogVersionUseCase {

    private final LoadCatalogVersionPort loadCatalogVersionPort;

    public GetCatalogVersionService(LoadCatalogVersionPort loadCatalogVersionPort) {
        this.loadCatalogVersionPort = loadCatalogVersionPort;
    }

    @Override
    public CatalogVersion execute(GetCatalogVersionCommand command) {
        if (command == null || command.branchId() == null || command.branchId().isBlank()) {
            throw new IllegalArgumentException("branchId is required");
        }
        return loadCatalogVersionPort.loadByBranchId(command.branchId());
    }
}
