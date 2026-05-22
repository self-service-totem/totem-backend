package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.totem.common.domain.exception.ConflictException;
import com.ffresco.totem.common.domain.exception.DomainValidationException;
import com.ffresco.totem.common.domain.exception.RateLimitExceededException;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;

public class ApiExceptionHandler {

    private final JsonApiResponseFactory responseFactory;

    public ApiExceptionHandler(JsonApiResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    public APIGatewayV2HTTPResponse handle(Exception exception) {
        if (exception instanceof IllegalArgumentException e) {
            return responseFactory.badRequest(e.getMessage());
        }
        if (exception instanceof ResourceNotFoundException e) {
            return responseFactory.notFound(e.getMessage());
        }
        if (exception instanceof ConflictException e) {
            return responseFactory.conflict(e.getMessage());
        }
        if (exception instanceof DomainValidationException e) {
            return responseFactory.unprocessableEntity(e.getMessage());
        }
        if (exception instanceof RateLimitExceededException e) {
            return responseFactory.tooManyRequests(e.getMessage());
        }
        return responseFactory.internalServerError("Internal server error");
    }
}
