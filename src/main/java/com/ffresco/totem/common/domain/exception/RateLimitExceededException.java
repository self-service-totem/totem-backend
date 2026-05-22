package com.ffresco.totem.common.domain.exception;

public class RateLimitExceededException extends DomainException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
