package com.ffresco.pricelist.application.port.in.catalog;

import com.ffresco.pricelist.domain.model.CatalogVersion;

public interface GetCatalogVersionUseCase {
    CatalogVersion execute(GetCatalogVersionCommand command);
}
