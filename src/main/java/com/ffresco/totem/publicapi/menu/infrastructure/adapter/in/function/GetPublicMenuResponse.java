package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function;

import java.util.List;

public record GetPublicMenuResponse(
        String branchId,
        String tableId,
        String currency,
        List<Category> categories
) {

    public record Category(
            String id,
            String name,
            List<Product> products
    ) {
    }

    public record Product(
            String id,
            String name,
            String description,
            Money price,
            boolean available
    ) {
    }

    public record Money(String amount, String currency) {
    }
}
