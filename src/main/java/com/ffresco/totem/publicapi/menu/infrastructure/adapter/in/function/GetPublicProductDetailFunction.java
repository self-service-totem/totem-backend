package com.ffresco.totem.publicapi.menu.infrastructure.adapter.in.function;

import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailUseCase;

import java.util.function.Function;

public class GetPublicProductDetailFunction
        implements Function<GetPublicProductDetailRequest, GetPublicProductDetailResponse> {

    private final GetPublicProductDetailUseCase getPublicProductDetailUseCase;

    public GetPublicProductDetailFunction(GetPublicProductDetailUseCase getPublicProductDetailUseCase) {
        this.getPublicProductDetailUseCase = getPublicProductDetailUseCase;
    }

    @Override
    public GetPublicProductDetailResponse apply(GetPublicProductDetailRequest request) {
        var command = PublicProductFunctionMapper.toCommand(request);
        var view = getPublicProductDetailUseCase.execute(command);
        return PublicProductFunctionMapper.toResponse(view);
    }
}
