package com.ffresco.totem.catalog.infrastructure.adapter.out.dynamodb;

import com.ffresco.totem.catalog.domain.model.CatalogVersion;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Maps DynamoDB item representations to domain objects.
 *
 * This mapper belongs to the outbound DynamoDB adapter. The domain model does
 * not know DynamoDB attributes, table keys, or mapping rules.
 */
final class DynamoDbCatalogVersionMapper {

    private static final String PK_PREFIX = "BRANCH#";
    private static final String SK_CATALOG_VERSION = "CATALOG#VERSION";

    private DynamoDbCatalogVersionMapper() {
    }

    static Map<String, AttributeValue> keyFromBranchId(String branchId) {
        return Map.of(
                "pk", AttributeValue.builder().s(PK_PREFIX + branchId).build(),
                "sk", AttributeValue.builder().s(SK_CATALOG_VERSION).build()
        );
    }

    static CatalogVersion toCatalogVersion(String requestedBranchId, Map<String, AttributeValue> item) {
        String branchId = stringValue(item, "branchId", requestedBranchId);
        String catalogVersion = requiredStringValue(item, "catalogVersion");
        String priceListId = requiredStringValue(item, "priceListId");

        try {
            return new CatalogVersion(branchId, Instant.parse(catalogVersion), priceListId);
        } catch (DateTimeParseException exception) {
            throw new IllegalStateException("Invalid catalogVersion stored in DynamoDB: " + catalogVersion, exception);
        }
    }

    private static String requiredStringValue(Map<String, AttributeValue> item, String attributeName) {
        String value = stringValue(item, attributeName, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("DynamoDB item is missing required attribute: " + attributeName);
        }
        return value;
    }

    private static String stringValue(Map<String, AttributeValue> item, String attributeName, String defaultValue) {
        AttributeValue value = item.get(attributeName);
        if (value == null || value.s() == null) {
            return defaultValue;
        }
        return value.s();
    }
}
