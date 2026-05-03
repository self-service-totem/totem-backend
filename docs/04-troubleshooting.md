# Troubleshooting

## Error: No qualifying bean of type ObjectMapper

Cause: the AWS/Lambda runtime does not load the web starter, so Spring may not auto-create an `ObjectMapper`.

Fix: keep an explicit bean:

```java
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
```

Then rebuild everything:

```bash
rm -rf .aws-sam
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json
```

## Error: Function timed out after 30 seconds

Possible causes:

- old JAR was mounted by SAM
- Spring cold start took longer than the timeout
- wrong function definition
- Lambda artifact includes unnecessary web dependencies

Recommended commands:

```bash
rm -rf .aws-sam
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json --debug
```

Recommended timeout while developing:

```yaml
Timeout: 90
```

## Error: no function found

Check `template.yaml`:

```yaml
SPRING_CLOUD_FUNCTION_DEFINITION: listPriceApiGateway
```

Check `application.yml` for local mode:

```yaml
spring:
  cloud:
    function:
      definition: listPrice
```
