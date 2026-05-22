package com.ffresco.totem.catalog.application.port.out;

import com.ffresco.totem.catalog.domain.model.CatalogVersion;

public interface LoadCatalogVersionPort {
    CatalogVersion loadByBranchId(String branchId);
}
