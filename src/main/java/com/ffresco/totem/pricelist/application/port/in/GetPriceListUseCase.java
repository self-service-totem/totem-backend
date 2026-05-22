package com.ffresco.totem.pricelist.application.port.in;

import com.ffresco.totem.pricelist.domain.model.PriceList;

public interface GetPriceListUseCase {
    PriceList execute(GetPriceListCommand command);
}
