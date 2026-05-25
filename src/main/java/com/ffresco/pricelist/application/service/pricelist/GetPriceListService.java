package com.ffresco.pricelist.application.service.pricelist;

import com.ffresco.pricelist.application.port.in.pricelist.GetPriceListCommand;
import com.ffresco.pricelist.application.port.in.pricelist.GetPriceListUseCase;
import com.ffresco.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.pricelist.domain.model.PriceList;

public class GetPriceListService implements GetPriceListUseCase {

    private final LoadProductsPort loadProductsPort;

    public GetPriceListService(LoadProductsPort loadProductsPort) {
        this.loadProductsPort = loadProductsPort;
    }

    @Override
    public PriceList execute(GetPriceListCommand command) {
        var products = loadProductsPort.loadByPriceListId(command.priceListId());
        return new PriceList(command.priceListId(), products);
    }
}
