package com.ffresco.pricelist.application.port.in.pricelist;

public record GetPriceListCommand(String priceListId) {

    public GetPriceListCommand {
        if (priceListId == null || priceListId.isBlank()) {
            priceListId = "default";
        }
    }
}
