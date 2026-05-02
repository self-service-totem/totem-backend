package com.ffresco.pricelist.application.port.in;

public record GetPriceListCommand(String priceListId) {

    public GetPriceListCommand {
        if (priceListId == null || priceListId.isBlank()) {
            priceListId = "default";
        }
    }
}
