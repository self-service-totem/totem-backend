package com.ffresco.pricelist.infrastructure.adapter.in.function.health;

import java.util.function.Supplier;

public class HealthFunction implements Supplier<String> {

    @Override
    public String get() {
        return "Ok";
    }
}
