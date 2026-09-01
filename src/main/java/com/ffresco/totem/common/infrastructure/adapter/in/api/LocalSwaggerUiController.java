package com.ffresco.totem.common.infrastructure.adapter.in.api;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Swagger UI for local testing only.
 *
 * Serves the already-bundled {@code openapi.yaml} and a Swagger UI page (via CDN)
 * so real endpoints can be exercised through LocalApiController without Postman/curl.
 *
 * Never active outside the "local" profile, which AWS/Lambda never activates.
 */
@RestController
@Profile("local")
public class LocalSwaggerUiController {

    private static final Path OPENAPI_FILE = Path.of("openapi.yaml");
    private static final String SWAGGER_UI_DIST_VERSION = "5.17.14";

    @GetMapping(value = "/openapi.yaml")
    public ResponseEntity<String> openapiYaml() throws IOException {
        if (!Files.exists(OPENAPI_FILE)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("openapi.yaml not found. Run 'make openapi-bundle' first.");
        }

        String content = Files.readString(OPENAPI_FILE, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/yaml"))
                .body(withLocalServerFirst(content));
    }

    /**
     * Prepends a "Local" server entry to the {@code servers} list of the served
     * copy only, so Swagger UI selects http://localhost:8080 by default. The
     * canonical openapi.yaml on disk (used by SAM/CI) is never modified.
     */
    private String withLocalServerFirst(String content) {
        return content.replaceFirst(
                "(?m)^servers:\\n",
                "servers:\n- url: http://localhost:8080\n  description: Local (mvn -Plocal spring-boot:run)\n"
        );
    }

    @GetMapping(value = "/docs")
    public ResponseEntity<String> swaggerUiPage() {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8" />
                    <title>Totem Backend - Local API Docs</title>
                    <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@%s/swagger-ui.css" />
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="https://unpkg.com/swagger-ui-dist@%s/swagger-ui-bundle.js"></script>
                    <script src="https://unpkg.com/swagger-ui-dist@%s/swagger-ui-standalone-preset.js"></script>
                    <script>
                        window.onload = function () {
                            window.ui = SwaggerUIBundle({
                                url: "/openapi.yaml",
                                dom_id: "#swagger-ui",
                                deepLinking: true,
                                presets: [
                                    SwaggerUIBundle.presets.apis,
                                    SwaggerUIStandalonePreset
                                ],
                                plugins: [
                                    SwaggerUIBundle.plugins.DownloadUrl
                                ],
                                layout: "StandaloneLayout"
                            });
                        };
                    </script>
                </body>
                </html>
                """.formatted(SWAGGER_UI_DIST_VERSION, SWAGGER_UI_DIST_VERSION, SWAGGER_UI_DIST_VERSION);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
