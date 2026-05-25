package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function;

import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuCommand;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class PublicMenuFunctionMapper {

    private PublicMenuFunctionMapper() {
    }

    static GetPublicMenuCommand toCommand(GetPublicMenuRequest request) {
        return new GetPublicMenuCommand(request == null ? null : request.tablePublicId());
    }

    static GetPublicMenuResponse toResponse(PublicMenuView view) {
        PublicMenu menu = view.menu();
        List<GetPublicMenuResponse.Category> categories = menu.categories().stream()
                .map(category -> new GetPublicMenuResponse.Category(
                        category.id(),
                        category.name(),
                        category.products().stream()
                                .map(product -> new GetPublicMenuResponse.Product(
                                        product.id(),
                                        product.name(),
                                        product.description(),
                                        new GetPublicMenuResponse.Money(
                                                formatAmount(product.price().amount()),
                                                product.price().currency().name()
                                        ),
                                        product.available()
                                ))
                                .toList()
                ))
                .toList();
        return new GetPublicMenuResponse(
                menu.branchId(),
                view.tableId(),
                menu.currency().name(),
                categories
        );
    }

    static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
