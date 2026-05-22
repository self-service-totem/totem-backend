package com.ffresco.totem.publicapi.menu.domain.exception;

import com.ffresco.totem.common.domain.exception.CodedDomainException;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;

public class PublicMenuNotFoundException extends ResourceNotFoundException implements CodedDomainException {

    public static final String CODE = "PUBLIC_MENU_NOT_FOUND";
    public static final String TITLE = "Public menu not found";

    public PublicMenuNotFoundException(String tenantId, String branchId) {
        super("No public menu was found for branch " + branchId + " in tenant " + tenantId + ".");
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
