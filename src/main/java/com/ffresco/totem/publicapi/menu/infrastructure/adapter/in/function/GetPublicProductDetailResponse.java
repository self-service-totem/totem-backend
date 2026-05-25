package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function;

public record GetPublicProductDetailResponse(
        String productId,
        String branchId,
        String tableId,
        String name,
        String description,
        Money price,
        boolean available,
        String categoryId,
        String categoryName
) {

    public record Money(String amount, String currency) {
    }
}
