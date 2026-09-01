package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.ffresco.totem.common.domain.exception.CodedDomainException;
import com.ffresco.totem.common.domain.exception.ConflictException;
import com.ffresco.totem.common.domain.exception.DomainValidationException;
import com.ffresco.totem.common.domain.exception.RateLimitExceededException;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final JsonApiResponseFactory responseFactory;

    public ApiExceptionHandler(JsonApiResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    public APIGatewayV2HTTPResponse handle(Exception exception) {
        if (exception instanceof IllegalArgumentException e) {
            return responseFactory.badRequest(codeOf(e), e.getMessage());
        }
        if (exception instanceof ResourceNotFoundException e) {
            return responseFactory.notFound(codeOf(e), titleOf(e), e.getMessage());
        }
        if (exception instanceof ConflictException e) {
            return responseFactory.conflict(codeOf(e), e.getMessage());
        }
        if (exception instanceof DomainValidationException e) {
            return responseFactory.unprocessableEntity(codeOf(e), e.getMessage());
        }
        if (exception instanceof RateLimitExceededException e) {
            return responseFactory.tooManyRequests(codeOf(e), e.getMessage());
        }
        log.error("Unhandled exception in API Gateway router", exception);
        return responseFactory.internalServerError("Internal server error");
    }

    private static String codeOf(Exception exception) {
        return exception instanceof CodedDomainException coded ? coded.code() : null;
    }

    private static String titleOf(Exception exception) {
        return exception instanceof CodedDomainException coded ? coded.title() : null;
    }
}
