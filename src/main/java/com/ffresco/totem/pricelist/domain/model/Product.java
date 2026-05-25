package com.ffresco.totem.pricelist.domain.model;

import com.ffresco.totem.common.domain.model.Money;
import java.util.Objects;

public record Product(String id, String name, Money price) {

    public Product {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(price, "price is required");
    }
}
