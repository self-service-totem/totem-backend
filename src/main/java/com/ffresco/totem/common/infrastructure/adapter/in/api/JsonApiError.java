package com.ffresco.totem.common.infrastructure.adapter.in.api;

public record JsonApiError(
        String status,
        String title,
        String detail
) {
}
