package com.ffresco.pricelist.infrastructure.adapter.in.api;

public record JsonApiResource<T>(
        String type,
        String id,
        T attributes
) {
}
