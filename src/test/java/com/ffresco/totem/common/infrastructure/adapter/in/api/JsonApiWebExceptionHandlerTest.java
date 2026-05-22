package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.ffresco.totem.common.domain.exception.CodedDomainException;
import com.ffresco.totem.common.domain.exception.ConflictException;
import com.ffresco.totem.common.domain.exception.DomainValidationException;
import com.ffresco.totem.common.domain.exception.RateLimitExceededException;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonApiWebExceptionHandlerTest {

    private final JsonApiWebExceptionHandler handler = new JsonApiWebExceptionHandler();

    @Test
    void shouldReturn400ForIllegalArgument() {
        var response = handler.handleBadRequest(new IllegalArgumentException("tableId is required"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/vnd.api+json");
        var error = firstError(response.getBody());
        assertThat(error.status()).isEqualTo("400");
        assertThat(error.title()).isEqualTo("Bad Request");
        assertThat(error.detail()).isEqualTo("tableId is required");
        assertThat(error.code()).isNull();
    }

    @Test
    void shouldReturn404WithCodedTitleAndCodeForCodedNotFound() {
        var exception = new CodedNotFound("PUBLIC_TABLE_NOT_FOUND", "Public table not found", "missing.");
        var response = handler.handleNotFound(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        var error = firstError(response.getBody());
        assertThat(error.status()).isEqualTo("404");
        assertThat(error.code()).isEqualTo("PUBLIC_TABLE_NOT_FOUND");
        assertThat(error.title()).isEqualTo("Public table not found");
        assertThat(error.detail()).isEqualTo("missing.");
    }

    @Test
    void shouldReturn404WithDefaultTitleForPlainNotFound() {
        var response = handler.handleNotFound(new ResourceNotFoundException("nope"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(firstError(response.getBody()).title()).isEqualTo("Not Found");
    }

    @Test
    void shouldReturn409ForConflict() {
        var response = handler.handleConflict(new ConflictException("conflict"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(firstError(response.getBody()).title()).isEqualTo("Conflict");
    }

    @Test
    void shouldReturn422ForDomainValidation() {
        var response = handler.handleUnprocessable(new DomainValidationException("bad"));

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(firstError(response.getBody()).title()).isEqualTo("Unprocessable Entity");
    }

    @Test
    void shouldReturn429ForRateLimit() {
        var response = handler.handleTooManyRequests(new RateLimitExceededException("slow down"));

        assertThat(response.getStatusCode().value()).isEqualTo(429);
        assertThat(firstError(response.getBody()).title()).isEqualTo("Too Many Requests");
    }

    @SuppressWarnings("unchecked")
    private static JsonApiError firstError(Map<String, Object> body) {
        assertThat(body).isNotNull();
        var errors = (List<JsonApiError>) body.get("errors");
        assertThat(errors).hasSize(1);
        return errors.get(0);
    }

    private static class CodedNotFound extends ResourceNotFoundException implements CodedDomainException {
        private final String code;
        private final String title;

        CodedNotFound(String code, String title, String message) {
            super(message);
            this.code = code;
            this.title = title;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String title() {
            return title;
        }
    }
}
