package com.ffresco.totem.common.infrastructure.adapter.in.api.health;

import com.ffresco.totem.common.infrastructure.adapter.in.api.JsonApiResource;
import com.ffresco.totem.common.infrastructure.adapter.in.function.health.HealthFunctionResponse;

public final class HealthJsonApiMapper {

    public static final String TYPE = "health";

    private HealthJsonApiMapper() {
    }

    public static JsonApiResource<HealthAttributes> toResource(HealthFunctionResponse response) {
        return new JsonApiResource<>(
                TYPE,
                TYPE,
                new HealthAttributes(response.status())
        );
    }
}
