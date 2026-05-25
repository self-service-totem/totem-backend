package com.ffresco.totem.pricelist.infrastructure.adapter.in.api;

import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.totem.pricelist.infrastructure.adapter.in.function.GetPriceListResponse;

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
