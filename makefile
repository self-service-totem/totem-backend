set-up-env-local:
	export AWS_REGION=sa-east-1
	export DYNAMODB_ENDPOINT=http://localhost:8000
	export TOTEM_CORE_TABLE_NAME=totem-core-local

dynamo-up-local:
	docker compose up -d dynamodb-local

dynamo-list-loal:
	AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy \
	aws dynamodb list-tables \
		--endpoint-url $(DYNAMODB_ENDPOINT) \
		--region $(AWS_REGION)

dynamo-create-table-local:
	AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy \
	aws dynamodb create-table \
		--endpoint-url $(DYNAMODB_ENDPOINT) \
		--region $(AWS_REGION) \
		--table-name $(TOTEM_CORE_TABLE_NAME) \
		--billing-mode PAY_PER_REQUEST \
		--attribute-definitions \
			AttributeName=pk,AttributeType=S \
			AttributeName=sk,AttributeType=S \
			AttributeName=gsi1pk,AttributeType=S \
			AttributeName=gsi1sk,AttributeType=S \
		--key-schema \
			AttributeName=pk,KeyType=HASH \
			AttributeName=sk,KeyType=RANGE \
		--global-secondary-indexes '[{"IndexName":"GSI1","KeySchema":[{"AttributeName":"gsi1pk","KeyType":"HASH"},{"AttributeName":"gsi1sk","KeyType":"RANGE"}],"Projection":{"ProjectionType":"ALL"}}]'

dynamo-seed-catalog-version-local:
	AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy \
	aws dynamodb put-item \
		--endpoint-url $(DYNAMODB_ENDPOINT) \
		--region $(AWS_REGION) \
		--table-name $(TOTEM_CORE_TABLE_NAME) \
		--item '{"pk":{"S":"BRANCH#branch-001"},"sk":{"S":"CATALOG#VERSION"},"branchId":{"S":"branch-001"},"catalogVersion":{"S":"2026-05-20T12:00:00Z"},"priceListId":{"S":"default"}}'

dynamo-seed-catalog-version-aws-stack-local:
	aws dynamodb put-item \
	  --region sa-east-1 \
	  --table-name totem-core-local \
	  --item '{"pk":{"S":"BRANCH#branch-001"},"sk":{"S":"CATALOG#VERSION"},"branchId":{"S":"branch-001"},"catalogVersion":{"S":"2026-05-20T12:00:00Z"},"priceListId":{"S":"default"}}' \
	  --profile ffresco

# Public table item (used by the GSI1 lookup)
dynamo-seed-table-item-version-aws-stack-local:
	AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy \
	aws dynamodb put-item \
	--endpoint-url http://localhost:8000 --region sa-east-1 \
	--table-name totem-core-local \
	--item '{"pk":{"S":"TENANT#tenant-001#PUBLIC_TABLE#tbl-public-001"},		"sk":{"S":"METADATA"},		"gsi1pk":{"S":"PUBLIC_TABLE#tbl-public-001"},		"gsi1sk":{"S":"TENANT#tenant-001#BRANCH#branch-001"},		"tenantId":{"S":"tenant-001"},		"branchId":{"S":"branch-001"},		"tableId":{"S":"tbl-001"},		"tablePublicId":{"S":"tbl-public-001"},		"status":{"S":"ACTIVE"}	}'

# Public menu materialized item
dynamo-seed-menu-materialized-version-aws-stack-local:
	AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy \
	aws dynamodb put-item \
	--endpoint-url http://localhost:8000 --region sa-east-1 \
	--table-name totem-core-local \
	--item '{"pk":{"S":"TENANT#tenant-001#BRANCH#branch-001"},"sk":{"S":"MENU#PUBLIC"},"tenantId":{"S":"tenant-001"},"branchId":{"S":"branch-001"},"currency":{"S":"BRL"},"categories":{"L":[	{"M":{		"id":{"S":"cat-burgers"},		"name":{"S":"Burgers"},	"products":{"L":[	{"M":{		"id":{"S":"prd-burger"},		"name":{"S":"Cheeseburger"},		"description":{"S":"Beef patty."},		"price":{"S":"12.50"},				"available":{"BOOL":true}			}}			]}		}}		]}	}'

openapi-bundle:
	python3 scripts/bundle-openapi.py openapi-src/openapi-root.yaml openapi.yaml

package: openapi-bundle
	mvn clean package

##### Seccion dedicada a git #####

sam-build: openapi-bundle
	sam build

# Check auth status using the ffresco GH CLI config profile.
gh-ffresco-status:
	GH_CONFIG_DIR=$$HOME/.config/gh-ffresco gh auth status

# Create a PR using variables passed at execution time.
# Usage:
# make gh-pr-create HEAD=feature/TOTEM-1-public-menu BODY_FILE=docs/tickets/generated/TOTEM-1-public-menu.md TITLE="TOTEM-1 Public Menu"
# Optional:
# BASE=develop GH_CONFIG_DIR_PATH=$$HOME/.config/gh-ffresco

gh-pr-create:
	@if [ -z "$(HEAD)" ]; then echo "Missing HEAD. Usage: make gh-pr-create HEAD=<branch> BODY_FILE=<file> [TITLE=<title>] [BASE=develop]"; exit 1; fi
	@if [ -z "$(BODY_FILE)" ]; then echo "Missing BODY_FILE. Usage: make gh-pr-create HEAD=<branch> BODY_FILE=<file> [TITLE=<title>] [BASE=develop]"; exit 1; fi
	@if [ ! -f "$(BODY_FILE)" ]; then echo "BODY_FILE not found: $(BODY_FILE)"; exit 1; fi
	GH_CONFIG_DIR=$${GH_CONFIG_DIR_PATH:-$$HOME/.config/gh-ffresco} gh pr create \
		--base $(if $(BASE),$(BASE),develop) \
		--head $(HEAD) \
		$(if $(TITLE),--title "$(TITLE)",) \
		--body-file $(BODY_FILE)

# Help: prints examples to invoke gh-pr-create.
gh-pr-create-help:
	@echo "Usage:"
	@echo "  make gh-pr-create HEAD=<branch> BODY_FILE=<file> [TITLE=<title>] [BASE=develop] [GH_CONFIG_DIR_PATH=<path>]"
	@echo ""
	@echo "Example:"
	@echo "  make gh-pr-create HEAD=feature/TOTEM-1-public-menu BODY_FILE=docs/tickets/generated/TOTEM-1-public-menu.md TITLE=\"TOTEM-1 Public Menu\""
