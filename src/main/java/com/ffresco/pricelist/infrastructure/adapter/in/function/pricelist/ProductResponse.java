package com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        String currency
) {
}
