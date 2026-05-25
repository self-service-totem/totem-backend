package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.api;

public record PublicMenuProductAttributes(
        String id,
        String name,
        String description,
        PublicMoneyAttributes price,
        boolean available
) {
}
