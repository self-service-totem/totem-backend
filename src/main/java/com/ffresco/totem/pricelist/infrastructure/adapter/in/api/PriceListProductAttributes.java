package com.ffresco.totem.pricelist.infrastructure.adapter.in.api;

import java.math.BigDecimal;

public record PriceListProductAttributes(
        String id,
        String name,
        BigDecimal price,
        String currency
) {
}
