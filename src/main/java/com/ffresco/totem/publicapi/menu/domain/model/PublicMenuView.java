package com.ffresco.totem.publicapi.menu.domain.model;

import java.util.Objects;

/**
 * Projection used by the menu listing endpoint. Bundles the filtered public
 * menu with the table/branch context resolved from the QR-encoded public
 * table id.
 */
public record PublicMenuView(
        String tablePublicId,
        String tableId,
        PublicMenu menu
) {
    public PublicMenuView {
        Objects.requireNonNull(tablePublicId, "tablePublicId is required");
        Objects.requireNonNull(tableId, "tableId is required");
        Objects.requireNonNull(menu, "menu is required");
    }
}
