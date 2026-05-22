package com.ffresco.totem.publicapi.menu.application.port.in;

import com.ffresco.totem.publicapi.menu.domain.model.PublicProductView;

public interface GetPublicProductDetailUseCase {
    PublicProductView execute(GetPublicProductDetailCommand command);
}
