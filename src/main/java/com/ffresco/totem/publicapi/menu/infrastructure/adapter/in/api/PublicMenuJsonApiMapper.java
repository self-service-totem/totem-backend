package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function.GetPublicMenuResponse;

public final class PublicMenuJsonApiMapper {

    public static final String TYPE = "public-menu";

    private PublicMenuJsonApiMapper() {
    }

    public static JsonApiResource<PublicMenuAttributes> toResource(GetPublicMenuResponse response) {
        var categories = response.categories().stream()
                .map(category -> new PublicMenuCategoryAttributes(
                        category.id(),
                        category.name(),
                        category.products().stream()
                                .map(product -> new PublicMenuProductAttributes(
                                        product.id(),
                                        product.name(),
                                        product.description(),
                                        new PublicMoneyAttributes(
                                                product.price().amount(),
                                                product.price().currency()
                                        ),
                                        product.available()
                                ))
                                .toList()
                ))
                .toList();

        var attributes = new PublicMenuAttributes(
                response.branchId(),
                response.tableId(),
                response.currency(),
                categories
        );

        return new JsonApiResource<>(TYPE, response.branchId() + "-menu", attributes);
    }
}
