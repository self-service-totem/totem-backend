package com.ffresco.totem.catalog.application.service;

import com.ffresco.totem.catalog.application.port.in.GetCatalogVersionCommand;
import com.ffresco.totem.catalog.application.port.in.GetCatalogVersionUseCase;
import com.ffresco.totem.catalog.application.port.out.LoadCatalogVersionPort;
import com.ffresco.totem.catalog.domain.model.CatalogVersion;

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
