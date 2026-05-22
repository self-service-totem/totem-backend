package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function;

import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailCommand;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import com.ffresco.totem.publicapi.menu.domain.model.PublicProductView;

final class PublicProductFunctionMapper {

    private PublicProductFunctionMapper() {
    }

    static GetPublicProductDetailCommand toCommand(GetPublicProductDetailRequest request) {
        if (request == null) {
            return new GetPublicProductDetailCommand(null, null);
        }
        return new GetPublicProductDetailCommand(request.tablePublicId(), request.productId());
    }

    static GetPublicProductDetailResponse toResponse(PublicProductView view) {
        PublicMenuProduct product = view.product();
        return new GetPublicProductDetailResponse(
                product.id(),
                view.branchId(),
                view.tableId(),
                product.name(),
                product.description(),
                new GetPublicProductDetailResponse.Money(
                        PublicMenuFunctionMapper.formatAmount(product.price().amount()),
                        product.price().currency().name()
                ),
                product.available(),
                view.categoryId(),
                view.categoryName()
        );
    }
}
