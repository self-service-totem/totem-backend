package com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist;

import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist.ListPriceResponse;

public final class PriceListJsonApiMapper {

    public static final String TYPE = "price-lists";

    private PriceListJsonApiMapper() {
    }

    public static JsonApiResource<PriceListAttributes> toResource(ListPriceResponse response) {
        return new JsonApiResource<>(
                TYPE,
                response.priceListId(),
                new PriceListAttributes(response.products())
        );
    }
}
