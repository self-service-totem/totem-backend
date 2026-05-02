package com.ffresco.pricelist.domain.model;

import com.ffresco.pricelist.domain.enums.Currency;
import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }
}
