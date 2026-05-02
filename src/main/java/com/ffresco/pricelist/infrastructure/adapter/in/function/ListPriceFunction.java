package com.ffresco.pricelist.infrastructure.adapter.in.function;

import com.ffresco.pricelist.application.port.in.GetPriceListCommand;
import com.ffresco.pricelist.application.port.in.GetPriceListUseCase;
import java.util.function.Function;

public class ListPriceFunction implements Function<ListPriceRequest, ListPriceResponse> {

    private final GetPriceListUseCase getPriceListUseCase;

    public ListPriceFunction(GetPriceListUseCase getPriceListUseCase) {
        this.getPriceListUseCase = getPriceListUseCase;
    }

    @Override
    public ListPriceResponse apply(ListPriceRequest request) {
        var command = new GetPriceListCommand(request == null ? null : request.priceListId());
        var priceList = getPriceListUseCase.execute(command);
        return ListPriceResponse.from(priceList);
    }
}

//TODO: crear un make para hacer subida a aws
