package com.ffresco.totem.publicapi.menu.application.port.out;

import com.ffresco.totem.publicapi.menu.domain.model.PublicTable;

public interface LoadPublicTablePort {
    PublicTable loadByPublicId(String tablePublicId);
}
