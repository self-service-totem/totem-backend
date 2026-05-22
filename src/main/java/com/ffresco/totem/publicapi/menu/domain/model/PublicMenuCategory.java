package com.ffresco.totem.publicapi.menu.domain.model;

import java.util.List;
import java.util.Objects;

public record PublicMenuCategory(
        String id,
        String name,
        List<PublicMenuProduct> products
) {
    public PublicMenuCategory {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(name, "name is required");
        products = products == null ? List.of() : List.copyOf(products);
    }

    public PublicMenuCategory withProducts(List<PublicMenuProduct> filtered) {
        return new PublicMenuCategory(id, name, filtered);
    }
}
