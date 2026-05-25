package com.ffresco.totem.pricelist.infrastructure.adapter.in.function;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        String currency
) {
}
