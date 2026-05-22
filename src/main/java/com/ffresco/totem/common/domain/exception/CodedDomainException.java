package com.ffresco.totem.common.domain.exception;

/**
 * Marker for domain exceptions that carry a stable error code and a human-readable
 * title to be rendered in the JSON:API error envelope alongside the HTTP status.
 *
 * Implementations are mapped centrally by ApiExceptionHandler.
 */
public interface CodedDomainException {

    String code();

    String title();
}
