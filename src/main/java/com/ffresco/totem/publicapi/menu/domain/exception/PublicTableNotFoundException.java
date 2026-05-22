package com.ffresco.totem.publicapi.menu.domain.exception;

import com.ffresco.totem.common.domain.exception.CodedDomainException;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;

public class PublicTableNotFoundException extends ResourceNotFoundException implements CodedDomainException {

    public static final String CODE = "PUBLIC_TABLE_NOT_FOUND";
    public static final String TITLE = "Public table not found";

    public PublicTableNotFoundException(String tablePublicId) {
        super("No public table was found for tableId " + tablePublicId + ".");
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
