package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonApiError(
        String status,
        String code,
        String title,
        String detail
) {
    public JsonApiError(String status, String title, String detail) {
        this(status, null, title, detail);
    }
}
