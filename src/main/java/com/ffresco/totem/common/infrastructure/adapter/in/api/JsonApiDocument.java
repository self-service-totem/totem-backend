package com.ffresco.totem.common.infrastructure.adapter.in.api;

/**
 * JSON:API top-level document.
 *
 * Used for request deserialization when POST/PATCH endpoints are added.
 * For GET endpoints, path/query parameters can be read from API Gateway directly.
 */
public record JsonApiDocument<T>(
        JsonApiResource<T> data
) {
}
