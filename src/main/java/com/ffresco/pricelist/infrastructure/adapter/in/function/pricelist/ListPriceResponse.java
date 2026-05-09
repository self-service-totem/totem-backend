package com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist;

import com.ffresco.pricelist.domain.model.PriceList;
import java.util.List;

public record ListPriceResponse(String priceListId, List<ProductResponse> products) {

    public static ListPriceResponse from(PriceList priceList) {
        var productResponses = priceList.products()
                .stream()
                .map(product -> new ProductResponse(
                        product.id(),
                        product.name(),
                        product.price().amount(),
                        product.price().currency().name()
                ))
                .toList();

        return new ListPriceResponse(priceList.id(), productResponses);
    }
}
