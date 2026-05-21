package com.ffresco.pricelist.infrastructure.adapter.in.function.pricelist;

import com.ffresco.pricelist.application.port.in.pricelist.GetPriceListCommand;
import com.ffresco.pricelist.domain.model.PriceList;

/**
 * Maps the Spring Cloud Function contract to/from application and domain objects.
 *
 * This mapper belongs to the inbound function adapter. The domain model does not
 * know request/response records and request/response records do not contain
 * domain conversion logic.
 */
final class PriceListFunctionMapper {

    private PriceListFunctionMapper() {
    }

    static GetPriceListCommand toCommand(GetPriceListRequest request) {
        return new GetPriceListCommand(request == null ? null : request.priceListId());
    }

    static GetPriceListResponse toResponse(PriceList priceList) {
        var productResponses = priceList.products()
                .stream()
                .map(product -> new ProductResponse(
                        product.id(),
                        product.name(),
                        product.price().amount(),
                        product.price().currency().name()
                ))
                .toList();

        return new GetPriceListResponse(priceList.id(), productResponses);
    }
}
