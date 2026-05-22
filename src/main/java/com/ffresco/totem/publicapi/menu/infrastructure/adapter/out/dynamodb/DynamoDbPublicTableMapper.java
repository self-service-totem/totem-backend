package com.ffresco.totem.publicapi.menu.infrastructure.adapter.out.dynamodb;

import com.ffresco.totem.publicapi.menu.domain.model.PublicTable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

final class DynamoDbPublicTableMapper {

    static final String GSI1_NAME = "GSI1";
    static final String GSI1_PK_PREFIX = "PUBLIC_TABLE#";

    static final String SK_METADATA = "METADATA";
    private static final String PK_PREFIX_TENANT = "TENANT#";
    private static final String PK_INFIX_PUBLIC_TABLE = "#PUBLIC_TABLE#";

    private DynamoDbPublicTableMapper() {
    }

    static String gsi1PkFromTablePublicId(String tablePublicId) {
        return GSI1_PK_PREFIX + tablePublicId;
    }

    static Map<String, AttributeValue> keyFor(String tenantId, String tablePublicId) {
        return Map.of(
                "pk", AttributeValue.builder().s(PK_PREFIX_TENANT + tenantId + PK_INFIX_PUBLIC_TABLE + tablePublicId).build(),
                "sk", AttributeValue.builder().s(SK_METADATA).build()
        );
    }

    static PublicTable toPublicTable(String requestedTablePublicId, Map<String, AttributeValue> item) {
        String tenantId = requiredString(item, "tenantId");
        String branchId = requiredString(item, "branchId");
        String tableId = requiredString(item, "tableId");
        String tablePublicId = stringValue(item, "tablePublicId", requestedTablePublicId);
        return new PublicTable(tenantId, branchId, tableId, tablePublicId);
    }

    private static String requiredString(Map<String, AttributeValue> item, String name) {
        String value = stringValue(item, name, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("DynamoDB public table item is missing required attribute: " + name);
        }
        return value;
    }

    private static String stringValue(Map<String, AttributeValue> item, String name, String defaultValue) {
        AttributeValue value = item.get(name);
        if (value == null || value.s() == null) {
            return defaultValue;
        }
        return value.s();
    }
}
