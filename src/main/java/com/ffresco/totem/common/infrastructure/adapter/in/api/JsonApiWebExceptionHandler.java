package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.ffresco.totem.common.domain.exception.CodedDomainException;
import com.ffresco.totem.common.domain.exception.ConflictException;
import com.ffresco.totem.common.domain.exception.DomainValidationException;
import com.ffresco.totem.common.domain.exception.RateLimitExceededException;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Spring MVC counterpart to {@link ApiExceptionHandler}.
 *
 * <p>Used only by the local execution mode ({@code mvn -Plocal spring-boot:run}),
 * where Spring Cloud Function Web exposes Function beans through Spring MVC and
 * never reaches the API Gateway router. Without this advice domain exceptions
 * surface as generic 500 responses with no JSON:API envelope.</p>
 *
 * <p>Mapping must stay in sync with {@link ApiExceptionHandler}.</p>
 */
@RestControllerAdvice
public class JsonApiWebExceptionHandler {

    private static final String CONTENT_TYPE = "application/vnd.api+json";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException exception) {
        return jsonApiError(HttpStatus.BAD_REQUEST, "Bad Request", exception);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException exception) {
        return jsonApiError(HttpStatus.NOT_FOUND, "Not Found", exception);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException exception) {
        return jsonApiError(HttpStatus.CONFLICT, "Conflict", exception);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Map<String, Object>> handleUnprocessable(DomainValidationException exception) {
        return jsonApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", exception);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(RateLimitExceededException exception) {
        return jsonApiError(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", exception);
    }

    private static ResponseEntity<Map<String, Object>> jsonApiError(
            HttpStatus status,
            String defaultTitle,
            Exception exception
    ) {
        String code = exception instanceof CodedDomainException coded ? coded.code() : null;
        String title = exception instanceof CodedDomainException coded && coded.title() != null
                ? coded.title()
                : defaultTitle;
        var error = new JsonApiError(String.valueOf(status.value()), code, title, exception.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(CONTENT_TYPE));

        return ResponseEntity.status(status)
                .headers(headers)
                .body(Map.of("errors", List.of(error)));
    }
}
