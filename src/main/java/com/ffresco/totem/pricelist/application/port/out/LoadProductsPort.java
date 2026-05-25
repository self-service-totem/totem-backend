package com.ffresco.totem.pricelist.application.port.out;

import com.ffresco.totem.pricelist.domain.model.Product;
import java.util.List;

public interface LoadProductsPort {
    List<Product> loadByPriceListId(String priceListId);
}
