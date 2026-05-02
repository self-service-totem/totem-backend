package com.ffresco.pricelist.application.port.out;

import com.ffresco.pricelist.domain.model.Product;
import java.util.List;

public interface LoadProductsPort {
    List<Product> loadByPriceListId(String priceListId);
}
