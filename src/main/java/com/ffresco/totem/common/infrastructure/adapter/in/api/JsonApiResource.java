package com.ffresco.totem.common.infrastructure.adapter.in.api;

public record JsonApiResource<T>(
        String type,
        String id,
        T attributes
) {
}
