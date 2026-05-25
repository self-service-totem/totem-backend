package com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist;

import java.util.List;

public record PriceListAttributes(
        List<PriceListProductAttributes> products
) {
}
