package com.ffresco.totem.publicapi.menu.infrastructure.adapter.out.dynamodb;

import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuCategory;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

final class DynamoDbPublicMenuMapper {

    static final String SK_PUBLIC_MENU = "MENU#PUBLIC";
    private static final String PK_PREFIX_TENANT = "TENANT#";
    private static final String PK_INFIX_BRANCH = "#BRANCH#";

    private DynamoDbPublicMenuMapper() {
    }

    static Map<String, AttributeValue> keyFor(String tenantId, String branchId) {
        return Map.of(
                "pk", AttributeValue.builder().s(PK_PREFIX_TENANT + tenantId + PK_INFIX_BRANCH + branchId).build(),
                "sk", AttributeValue.builder().s(SK_PUBLIC_MENU).build()
        );
    }

    static PublicMenu toPublicMenu(String requestedTenantId, String requestedBranchId, Map<String, AttributeValue> item) {
        String tenantId = stringValue(item, "tenantId", requestedTenantId);
        String branchId = stringValue(item, "branchId", requestedBranchId);
        Currency currency = Currency.valueOf(requiredString(item, "currency"));

        List<PublicMenuCategory> categories = listValue(item, "categories").stream()
                .map(AttributeValue::m)
                .map(categoryMap -> toCategory(categoryMap, currency))
                .toList();

        return new PublicMenu(tenantId, branchId, currency, categories);
    }

    private static PublicMenuCategory toCategory(Map<String, AttributeValue> categoryMap, Currency currency) {
        String id = requiredString(categoryMap, "id");
        String name = requiredString(categoryMap, "name");
        List<PublicMenuProduct> products = listValue(categoryMap, "products").stream()
                .map(AttributeValue::m)
                .map(productMap -> toProduct(productMap, currency))
                .toList();
        return new PublicMenuCategory(id, name, products);
    }

    private static PublicMenuProduct toProduct(Map<String, AttributeValue> productMap, Currency currency) {
        String id = requiredString(productMap, "id");
        String name = requiredString(productMap, "name");
        String description = stringValue(productMap, "description", null);
        BigDecimal amount = new BigDecimal(requiredString(productMap, "price"));
        boolean available = booleanValue(productMap, "available", true);
        return new PublicMenuProduct(id, name, description, new Money(amount, currency), available);
    }

    private static List<AttributeValue> listValue(Map<String, AttributeValue> item, String name) {
        AttributeValue value = item.get(name);
        if (value == null || value.l() == null) {
            return List.of();
        }
        return value.l();
    }

    private static String requiredString(Map<String, AttributeValue> item, String name) {
        String value = stringValue(item, name, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("DynamoDB public menu item is missing required attribute: " + name);
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

    private static boolean booleanValue(Map<String, AttributeValue> item, String name, boolean defaultValue) {
        AttributeValue value = item.get(name);
        if (value == null || value.bool() == null) {
            return defaultValue;
        }
        return value.bool();
    }
}
