# Troubleshooting

## No qualifying bean of type ObjectMapper

Cause: Lambda runtime may not auto-create an `ObjectMapper`.

Fix: keep explicit `JacksonConfig`.

```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
```

## Function timed out

Possible causes:

```text
old JAR mounted by SAM
Spring cold start longer than timeout
wrong function definition
too many dependencies
```

Recommended rebuild:

```bash
rm -rf .aws-sam
mvn clean package
sam build --cached=false
sam local invoke PriceListFunction -e events/api-gateway-get-price-list.json --debug
```

Recommended development timeout:

```yaml
Timeout: 90
```

## No function found

Check AWS/SAM:

```text
SPRING_CLOUD_FUNCTION_DEFINITION=apiGatewayRouter
```

Check local direct mode in `application.yml`:

```yaml
spring:
  cloud:
    function:
      definition: listPrice
```
