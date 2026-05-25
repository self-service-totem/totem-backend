package com.ffresco.totem.pricelist.infrastructure.adapter.in.function;

import java.util.List;

public record GetPriceListResponse(String priceListId, List<ProductResponse> products) {
}
