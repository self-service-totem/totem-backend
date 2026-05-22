package com.ffresco.totem.catalog.infrastructure.adapter.out.dynamodb;

import com.ffresco.totem.catalog.application.port.out.LoadCatalogVersionPort;
import com.ffresco.totem.common.domain.exception.ResourceNotFoundException;
import com.ffresco.totem.catalog.domain.model.CatalogVersion;
import org.springframework.beans.factory.annotation.Qualifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;


public class DynamoDbCatalogVersionAdapter implements LoadCatalogVersionPort {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbCatalogVersionAdapter(
            DynamoDbClient dynamoDbClient,
            @Qualifier("totemCoreTableName") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public CatalogVersion loadByBranchId(String branchId) {
        var request = GetItemRequest.builder()
                .tableName(tableName)
                .key(DynamoDbCatalogVersionMapper.keyFromBranchId(branchId))
                .consistentRead(true)
                .build();

        var response = dynamoDbClient.getItem(request);
        if (!response.hasItem() || response.item().isEmpty()) {
            throw new ResourceNotFoundException("Catalog version not found for branchId: " + branchId);
        }

        return DynamoDbCatalogVersionMapper.toCatalogVersion(branchId, response.item());
    }
}
