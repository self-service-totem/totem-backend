package com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist;

import com.ffresco.pricelist.application.port.in.pricelist.GetPriceListUseCase;
import java.util.function.Function;

/**
 * Spring Cloud Function adapter for the get-price-list use case.
 *
 * This class is an infrastructure inbound adapter. It does not know HTTP,
 * API Gateway, JSON:API, or response status codes. It only translates the
 * function contract into the application use case contract.
 */
public class GetPriceListFunction implements Function<GetPriceListRequest, GetPriceListResponse> {

    private final GetPriceListUseCase getPriceListUseCase;

    public GetPriceListFunction(GetPriceListUseCase getPriceListUseCase) {
        this.getPriceListUseCase = getPriceListUseCase;
    }

    @Override
    public GetPriceListResponse apply(GetPriceListRequest request) {
        var command = PriceListFunctionMapper.toCommand(request);
        var priceList = getPriceListUseCase.execute(command);
        return PriceListFunctionMapper.toResponse(priceList);
    }
}
