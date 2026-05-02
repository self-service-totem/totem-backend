package com.ffresco.pricelist.domain.model;

import java.util.List;
import java.util.Objects;

public record PriceList(String id, List<Product> products) {

    public PriceList {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(products, "products is required");
        products = List.copyOf(products);
    }
}
