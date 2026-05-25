package com.ffresco.totem.pricelist.infrastructure.adapter.in.api;

import java.util.List;

public record PriceListAttributes(
        List<PriceListProductAttributes> products
) {
}
