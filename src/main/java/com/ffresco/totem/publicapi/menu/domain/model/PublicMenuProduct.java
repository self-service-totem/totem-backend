package com.ffresco.totem.publicapi.menu.domain.model;

import com.ffresco.totem.common.domain.model.Money;

import java.util.Objects;

public record PublicMenuProduct(
        String id,
        String name,
        String description,
        Money price,
        boolean available
) {
    public PublicMenuProduct {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(price, "price is required");
    }
}
