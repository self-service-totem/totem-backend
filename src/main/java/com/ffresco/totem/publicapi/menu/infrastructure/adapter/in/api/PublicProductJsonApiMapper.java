package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicProductDetailResponse;

public final class PublicProductJsonApiMapper {

    public static final String TYPE = "public-product";

    private PublicProductJsonApiMapper() {
    }

    public static JsonApiResource<PublicProductAttributes> toResource(GetPublicProductDetailResponse response) {
        var attributes = new PublicProductAttributes(
                response.branchId(),
                response.tableId(),
                response.name(),
                response.description(),
                new PublicMoneyAttributes(
                        response.price().amount(),
                        response.price().currency()
                ),
                response.available(),
                response.categoryId(),
                response.categoryName()
        );
        return new JsonApiResource<>(TYPE, response.productId(), attributes);
    }
}
