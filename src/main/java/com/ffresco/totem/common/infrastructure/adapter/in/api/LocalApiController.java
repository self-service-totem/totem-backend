package com.ffresco.totem.common.infrastructure.adapter.in.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Profile("local")
public class LocalApiController {

    private final Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> apiGatewayRouter;

    public LocalApiController(
            @Qualifier("apiGatewayRouter") Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> apiGatewayRouter
    ) {
        this.apiGatewayRouter = apiGatewayRouter;
    }

    @RequestMapping(
            path = "/**",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE
            }
    )
    public ResponseEntity<String> handle(HttpServletRequest request) throws IOException {
        APIGatewayV2HTTPEvent event = toApiGatewayEvent(request);
        APIGatewayV2HTTPResponse response = apiGatewayRouter.apply(event);
        return toResponseEntity(response);
    }

    private APIGatewayV2HTTPEvent toApiGatewayEvent(HttpServletRequest request) throws IOException {
        String path = pathWithoutContextPath(request);
        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setRouteKey(request.getMethod() + " " + path);
        event.setRawPath(path);
        event.setRawQueryString(request.getQueryString());
        event.setHeaders(headers(request));
        event.setQueryStringParameters(queryParameters(request));
        event.setBody(body.isBlank() ? null : body);
        event.setIsBase64Encoded(false);
        return event;
    }

    private String pathWithoutContextPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        return path == null || path.isBlank() ? "/" : path;
    }

    private Map<String, String> headers(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(name -> name, request::getHeader, (left, right) -> right));
    }

    private Map<String, String> queryParameters(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().length > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
    }

    private ResponseEntity<String> toResponseEntity(APIGatewayV2HTTPResponse response) {
        HttpHeaders headers = new HttpHeaders();

        if (response.getHeaders() != null) {
            response.getHeaders().forEach(headers::add);
        }

        return new ResponseEntity<>(
                response.getBody(),
                headers,
                HttpStatusCode.valueOf(response.getStatusCode())
        );
    }
}
