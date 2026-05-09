package com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist;

import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist.GetPriceListResponse;

public final class PriceListJsonApiMapper {

    public static final String TYPE = "price-lists";

    private PriceListJsonApiMapper() {
    }

    public static JsonApiResource<PriceListAttributes> toResource(GetPriceListResponse response) {
        var products = response.products()
                .stream()
                .map(product -> new PriceListProductAttributes(
                        product.id(),
                        product.name(),
                        product.price(),
                        product.currency()
                ))
                .toList();

        return new JsonApiResource<>(
                TYPE,
                response.priceListId(),
                new PriceListAttributes(products)
        );
    }
}
