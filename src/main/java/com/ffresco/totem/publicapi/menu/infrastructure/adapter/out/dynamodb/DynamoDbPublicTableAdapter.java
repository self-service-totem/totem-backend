package com.ffresco.totem.publicapi.menu.infrastructure.adapter.out.dynamodb;

import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicTablePort;
import com.ffresco.totem.publicapi.menu.domain.exception.PublicTableNotFoundException;
import com.ffresco.totem.publicapi.menu.domain.model.PublicTable;
import org.springframework.beans.factory.annotation.Qualifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.Map;

public class DynamoDbPublicTableAdapter implements LoadPublicTablePort {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbPublicTableAdapter(
            DynamoDbClient dynamoDbClient,
            @Qualifier("totemCoreTableName") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public PublicTable loadByPublicId(String tablePublicId) {
        var request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(DynamoDbPublicTableMapper.GSI1_NAME)
                .keyConditionExpression("gsi1pk = :gsi1pk")
                .expressionAttributeValues(Map.of(
                        ":gsi1pk", AttributeValue.builder()
                                .s(DynamoDbPublicTableMapper.gsi1PkFromTablePublicId(tablePublicId))
                                .build()
                ))
                .limit(1)
                .build();

        var response = dynamoDbClient.query(request);
        if (response.items() == null || response.items().isEmpty()) {
            throw new PublicTableNotFoundException(tablePublicId);
        }

        return DynamoDbPublicTableMapper.toPublicTable(tablePublicId, response.items().get(0));
    }
}
