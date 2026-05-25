package com.ffresco.totem.publicapi.menu.application.service;

import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuCommand;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuUseCase;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicMenuPort;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicTablePort;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuView;
import com.ffresco.totem.publicapi.menu.domain.model.PublicTable;

public class GetPublicMenuUseCaseImpl implements GetPublicMenuUseCase {

    private final LoadPublicTablePort loadPublicTablePort;
    private final LoadPublicMenuPort loadPublicMenuPort;

    public GetPublicMenuUseCaseImpl(
            LoadPublicTablePort loadPublicTablePort,
            LoadPublicMenuPort loadPublicMenuPort
    ) {
        this.loadPublicTablePort = loadPublicTablePort;
        this.loadPublicMenuPort = loadPublicMenuPort;
    }

    @Override
    public PublicMenuView execute(GetPublicMenuCommand command) {
        if (command == null || command.tablePublicId() == null || command.tablePublicId().isBlank()) {
            throw new IllegalArgumentException("tableId is required");
        }

        PublicTable publicTable = loadPublicTablePort.loadByPublicId(command.tablePublicId());
        var menu = loadPublicMenuPort.loadByBranch(publicTable.tenantId(), publicTable.branchId())
                .availableOnly();

        return new PublicMenuView(
                publicTable.tablePublicId(),
                publicTable.tableId(),
                menu
        );
    }
}
