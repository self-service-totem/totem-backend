package com.ffresco.pricelist.infrastructure.adapter.in.api;

public record JsonApiError(
        String status,
        String title,
        String detail
) {
}
