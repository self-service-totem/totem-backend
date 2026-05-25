package com.ffresco.totem.publicapi.menu.application.port.out;

import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;

public interface LoadPublicMenuPort {
    PublicMenu loadByBranch(String tenantId, String branchId);
}
