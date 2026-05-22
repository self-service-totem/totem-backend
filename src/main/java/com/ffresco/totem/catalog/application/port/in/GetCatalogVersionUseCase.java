package com.ffresco.totem.catalog.application.port.in;

import com.ffresco.totem.catalog.domain.model.CatalogVersion;

public interface GetCatalogVersionUseCase {
    CatalogVersion execute(GetCatalogVersionCommand command);
}
