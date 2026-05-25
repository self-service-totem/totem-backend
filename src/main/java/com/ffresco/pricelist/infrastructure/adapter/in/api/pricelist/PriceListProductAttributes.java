package com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist;

import java.math.BigDecimal;

public record PriceListProductAttributes(
        String id,
        String name,
        BigDecimal price,
        String currency
) {
}
