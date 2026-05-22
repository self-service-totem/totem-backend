package com.ffresco.totem.common.infrastructure.adapter.in.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Forces {@code Content-Type: application/vnd.api+json} on every Spring MVC
 * success response.
 *
 * <p>Mirrors what {@link JsonApiResponseFactory} already does for the
 * Lambda/API Gateway path and what {@link JsonApiWebExceptionHandler} does for
 * error responses, closing the gap on success responses produced by Spring
 * Cloud Function Web (the local {@code mvn -Plocal spring-boot:run} entry
 * point). Inert under {@code web-application-type: none} (Lambda).</p>
 */
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JsonApiContentTypeAdvice implements ResponseBodyAdvice<Object> {

    private static final MediaType JSON_API = MediaType.parseMediaType("application/vnd.api+json");

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        response.getHeaders().setContentType(JSON_API);
        return body;
    }
}
