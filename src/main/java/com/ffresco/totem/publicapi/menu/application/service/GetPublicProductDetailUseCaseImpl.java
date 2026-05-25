package com.ffresco.totem.publicapi.menu.application.service;

import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailCommand;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailUseCase;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicMenuPort;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicTablePort;
import com.ffresco.totem.publicapi.menu.domain.exception.PublicProductNotFoundException;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuCategory;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import com.ffresco.totem.publicapi.menu.domain.model.PublicProductView;
import com.ffresco.totem.publicapi.menu.domain.model.PublicTable;

public class GetPublicProductDetailUseCaseImpl implements GetPublicProductDetailUseCase {

    private final LoadPublicTablePort loadPublicTablePort;
    private final LoadPublicMenuPort loadPublicMenuPort;

    public GetPublicProductDetailUseCaseImpl(
            LoadPublicTablePort loadPublicTablePort,
            LoadPublicMenuPort loadPublicMenuPort
    ) {
        this.loadPublicTablePort = loadPublicTablePort;
        this.loadPublicMenuPort = loadPublicMenuPort;
    }

    @Override
    public PublicProductView execute(GetPublicProductDetailCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.tablePublicId() == null || command.tablePublicId().isBlank()) {
            throw new IllegalArgumentException("tableId is required");
        }
        if (command.productId() == null || command.productId().isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }

        PublicTable publicTable = loadPublicTablePort.loadByPublicId(command.tablePublicId());
        PublicMenu menu = loadPublicMenuPort
                .loadByBranch(publicTable.tenantId(), publicTable.branchId())
                .availableOnly();

        for (PublicMenuCategory category : menu.categories()) {
            for (PublicMenuProduct product : category.products()) {
                if (product.id().equals(command.productId())) {
                    return new PublicProductView(
                            publicTable.tenantId(),
                            publicTable.branchId(),
                            publicTable.tableId(),
                            publicTable.tablePublicId(),
                            category.id(),
                            category.name(),
                            product
                    );
                }
            }
        }

        throw new PublicProductNotFoundException(command.productId());
    }
}
