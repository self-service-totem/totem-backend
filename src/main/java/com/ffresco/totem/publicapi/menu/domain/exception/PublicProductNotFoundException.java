package com.ffresco.totem.publicapi.menu.domain.exception;

import com.ffresco.totem.common.domain.exception.CodedDomainException;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;

public class PublicProductNotFoundException extends ResourceNotFoundException implements CodedDomainException {

    public static final String CODE = "PUBLIC_PRODUCT_NOT_FOUND";
    public static final String TITLE = "Public product not found";

    public PublicProductNotFoundException(String productId) {
        super("Product " + productId + " is not available in the public menu for this table.");
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public String title() {
        return TITLE;
    }
}
