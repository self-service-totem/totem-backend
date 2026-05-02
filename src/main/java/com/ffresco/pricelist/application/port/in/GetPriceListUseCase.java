package com.ffresco.pricelist.application.port.in;

import com.ffresco.pricelist.domain.model.PriceList;

public interface GetPriceListUseCase {
    PriceList execute(GetPriceListCommand command);
}
