package com.ffresco.totem.pricelist.infrastructure.adapter.out.memory;

import com.ffresco.totem.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.pricelist.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;

public class InMemoryProductAdapter implements LoadProductsPort {

    @Override
    public List<Product> loadByPriceListId(String priceListId) {
        return List.of(
                new Product("P-001", "Café Especial 500g", new Money(new BigDecimal("12.90"), Currency.USD)),
                new Product("P-002", "Yerba Mate Premium 1kg", new Money(new BigDecimal("8.50"), Currency.USD))
        );
    }
}
