package com.ffresco.totem.pricelist.application.service;

import com.ffresco.totem.pricelist.application.port.in.GetPriceListCommand;
import com.ffresco.totem.pricelist.application.port.in.GetPriceListUseCase;
import com.ffresco.totem.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.totem.pricelist.domain.model.PriceList;

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
