package com.ffresco.pricelist.application.service;

import com.ffresco.pricelist.application.port.in.pricelist.GetPriceListCommand;
import com.ffresco.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.pricelist.application.service.pricelist.GetPriceListService;
import com.ffresco.pricelist.domain.enums.Currency;
import com.ffresco.pricelist.domain.model.Money;
import com.ffresco.pricelist.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetPriceListServiceTest {

    @Test
    void shouldReturnPriceListWithProducts() {
        LoadProductsPort loadProductsPort = priceListId -> List.of(
                new Product("P-001", "Test Product", new Money(new BigDecimal("10.00"), Currency.USD))
        );
        var service = new GetPriceListService(loadProductsPort);

        var result = service.execute(new GetPriceListCommand("default"));

        assertThat(result.id()).isEqualTo("default");
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().get(0).name()).isEqualTo("Test Product");
    }
}
