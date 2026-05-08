package com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist;

import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.pricelist.infrastructure.adapter.in.function.ListPriceResponse;

public final class PriceListResource {

    public static final String TYPE = "price-lists";

    private PriceListResource() {
    }

    public static JsonApiResource<PriceListAttributes> from(ListPriceResponse response) {
        return new JsonApiResource<>(
                TYPE,
                response.priceListId(),
                new PriceListAttributes(response.products())
        );
    }
}
