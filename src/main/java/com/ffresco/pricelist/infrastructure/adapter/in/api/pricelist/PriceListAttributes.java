package com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist;

import com.ffresco.pricelist.infrastructure.adapter.in.function.ProductResponse;

import java.util.List;

public record PriceListAttributes(
        List<ProductResponse> products
) {
}
