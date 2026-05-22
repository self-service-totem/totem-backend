package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function;

import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuUseCase;

import java.util.function.Function;

public class GetPublicMenuFunction implements Function<GetPublicMenuRequest, GetPublicMenuResponse> {

    private final GetPublicMenuUseCase getPublicMenuUseCase;

    public GetPublicMenuFunction(GetPublicMenuUseCase getPublicMenuUseCase) {
        this.getPublicMenuUseCase = getPublicMenuUseCase;
    }

    @Override
    public GetPublicMenuResponse apply(GetPublicMenuRequest request) {
        var command = PublicMenuFunctionMapper.toCommand(request);
        var view = getPublicMenuUseCase.execute(command);
        return PublicMenuFunctionMapper.toResponse(view);
    }
}
