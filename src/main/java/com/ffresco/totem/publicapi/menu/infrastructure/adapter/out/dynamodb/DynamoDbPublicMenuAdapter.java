package com.ffresco.totem.publicapi.menu.infrastructure.adapter.out.dynamodb;

import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicMenuPort;
import com.ffresco.totem.publicapi.menu.domain.exception.PublicMenuNotFoundException;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import org.springframework.beans.factory.annotation.Qualifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

public class DynamoDbPublicMenuAdapter implements LoadPublicMenuPort {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbPublicMenuAdapter(
            DynamoDbClient dynamoDbClient,
            @Qualifier("totemCoreTableName") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public PublicMenu loadByBranch(String tenantId, String branchId) {
        var request = GetItemRequest.builder()
                .tableName(tableName)
                .key(DynamoDbPublicMenuMapper.keyFor(tenantId, branchId))
                .consistentRead(true)
                .build();

        var response = dynamoDbClient.getItem(request);
        if (!response.hasItem() || response.item().isEmpty()) {
            throw new PublicMenuNotFoundException(tenantId, branchId);
        }

        return DynamoDbPublicMenuMapper.toPublicMenu(tenantId, branchId, response.item());
    }
}
