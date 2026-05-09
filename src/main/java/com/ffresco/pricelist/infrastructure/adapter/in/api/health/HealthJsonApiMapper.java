package com.ffresco.pricelist.infrastructure.adapter.in.api.health;

import com.ffresco.pricelist.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.pricelist.infrastructure.adapter.in.api.pricelist.PriceListAttributes;
import com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist.ListPriceResponse;

public final class HealthJsonApiMapper {

    public static final String TYPE = "health";

    private HealthJsonApiMapper() {
    }

    public static JsonApiResource<String> toResource(String response) {
        return new JsonApiResource<>(
                TYPE,
                TYPE,
                response
        );
    }
}
