package com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist;

import java.util.List;

public record GetPriceListResponse(String priceListId, List<ProductResponse> products) {
}
