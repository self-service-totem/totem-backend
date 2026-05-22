package com.ffresco.totem.publicapi.menu.domain.model;

import java.util.Objects;

/**
 * Projection used by the product detail endpoint. Bundles the product with
 * the category it belongs to and the table/branch context resolved from the
 * QR-encoded public table id.
 */
public record PublicProductView(
        String tenantId,
        String branchId,
        String tableId,
        String tablePublicId,
        String categoryId,
        String categoryName,
        PublicMenuProduct product
) {
    public PublicProductView {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(branchId, "branchId is required");
        Objects.requireNonNull(tableId, "tableId is required");
        Objects.requireNonNull(tablePublicId, "tablePublicId is required");
        Objects.requireNonNull(categoryId, "categoryId is required");
        Objects.requireNonNull(categoryName, "categoryName is required");
        Objects.requireNonNull(product, "product is required");
    }
}
