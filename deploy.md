## Crear la lambda ************

### Build MAVEN
```bash
mvn clean package
```

### BUILD CON SAM CLI (puedo usar sam build, pero falla `sam build` por version ver de bajar una nueva)
```bash
sam build
```

### Deploy 
#### Default
```bash
sam validate 
sam deploy --config-env local --profile ffresco  ##deploy default
```
#### Ambientes
```bash
sam deploy --config-env dev --profile ffresco
sam deploy --config-env prod --profile ffresco
```

## Invocar *************
### remoto apuntando aws
```bash
aws lambda invoke --profile ffresco \
  --function-name price-list-lambda-sam \
  --payload '{"priceListId":"default"}' \
  response.json \
  --cli-binary-format raw-in-base64-out
```
### invocar  local 
#### Configurar Docker Host (Colima) necesario antes de los otros comandos de local
```bash
export DOCKER_HOST=unix:///Users/fernandofresco/.colima/default/docker.sock
```
##### Invocar la lambda
```bash
sam local invoke PriceListFunction --parameter-overrides Environment=local ReleaseVersion=local --event events/list-price-local.json
```
##### Invocar a traves de API Gateway local
```bash
sam local start-api --parameter-overrides Environment=local ReleaseVersion=local --profile ffresco 
```

## Utiles ************

### Sync solo codigo (sin desplegar infraestructura)
```bash
sam sync --code --stack-name price-list-lambda-stack --profile ffresco
```

### Sync en watch (sincroniza cuando cambia codigo)
```bash
sam sync --code --watch --stack-name price-list-lambda-stack --profile ffresco
```

### Validar openapi
```bash
npx @redocly/cli lint openapi.yaml   
```
