package com.ffresco.totem.publicapi.menu.domain.model;

import java.util.Objects;

public record PublicTable(
        String tenantId,
        String branchId,
        String tableId,
        String tablePublicId
) {
    public PublicTable {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(branchId, "branchId is required");
        Objects.requireNonNull(tableId, "tableId is required");
        Objects.requireNonNull(tablePublicId, "tablePublicId is required");
    }
}
