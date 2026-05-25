package com.ffresco.totem.common.infrastructure.adapter.in.function.health;

import java.util.function.Supplier;

public class HealthFunction implements Supplier<HealthFunctionResponse> {

    @Override
    public HealthFunctionResponse get() {
        return new HealthFunctionResponse("ok");
    }
}
