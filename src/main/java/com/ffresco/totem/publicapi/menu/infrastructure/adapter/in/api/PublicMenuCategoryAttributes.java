package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

import java.util.List;

public record PublicMenuCategoryAttributes(
        String id,
        String name,
        List<PublicMenuProductAttributes> products
) {
}
