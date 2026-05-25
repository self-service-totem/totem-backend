package com.ffresco.totem.pricelist.application.service;

import com.ffresco.totem.pricelist.application.port.in.GetPriceListCommand;
import com.ffresco.totem.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.pricelist.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetPriceListUseCaseImplTest {

    @Test
    void shouldReturnPriceListWithProducts() {
        LoadProductsPort loadProductsPort = priceListId -> List.of(
                new Product("P-001", "Test Product", new Money(new BigDecimal("10.00"), Currency.USD))
        );
        var service = new GetPriceListUseCaseImpl(loadProductsPort);

        var result = service.execute(new GetPriceListCommand("default"));

        assertThat(result.id()).isEqualTo("default");
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().get(0).name()).isEqualTo("Test Product");
    }
}
