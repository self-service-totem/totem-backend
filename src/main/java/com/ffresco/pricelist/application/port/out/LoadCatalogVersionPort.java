package com.ffresco.pricelist.application.port.out;

import com.ffresco.pricelist.domain.model.CatalogVersion;

public interface LoadCatalogVersionPort {
    CatalogVersion loadByBranchId(String branchId);
}
