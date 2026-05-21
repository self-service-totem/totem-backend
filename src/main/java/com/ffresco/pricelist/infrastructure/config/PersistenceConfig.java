package com.ffresco.pricelist.infrastructure.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class PersistenceConfig {

    @Bean
    @Profile("!local")
    public DynamoDbClient dynamoDbClient(
            @Value("${app.aws.region:${AWS_REGION:sa-east-1}}") String region
    ) {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("local")
    public DynamoDbClient localDynamoDbClient(
            @Value("${app.aws.region:${AWS_REGION:sa-east-1}}") String region,
            @Value("${app.aws.dynamodb.endpoint:${app.dynamodb.endpoint:http://localhost:8000}}") String endpoint
    ) {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummy", "dummy")
                        )
                )
                .build();
    }

    @Bean
    public String totemCoreTableName(
            @Value("${app.aws.dynamodb.table-name:${app.dynamodb.table-name:totem-core-dev}}") String tableName
    ) {
        return tableName;
    }
}
