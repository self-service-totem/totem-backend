package com.ffresco.totem.publicapi.menu.application.port.in;

import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuView;

public interface GetPublicMenuUseCase {
    PublicMenuView execute(GetPublicMenuCommand command);
}
