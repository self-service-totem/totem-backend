AWS_REGION=sa-east-1
DYNAMODB_ENDPOINT=http://localhost:8000
TOTEM_CORE_TABLE_NAME=totem-core-local

dynamo-up:
	docker compose up -d dynamodb-local

dynamo-list:
	AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy \
	aws dynamodb list-tables \
		--endpoint-url $(DYNAMODB_ENDPOINT) \
		--region $(AWS_REGION)

dynamo-create-table:
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

dynamo-seed-catalog-version:
	AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy \
	aws dynamodb put-item \
		--endpoint-url $(DYNAMODB_ENDPOINT) \
		--region $(AWS_REGION) \
		--table-name $(TOTEM_CORE_TABLE_NAME) \
		--item '{"pk":{"S":"BRANCH#branch-001"},"sk":{"S":"CATALOG#VERSION"},"branchId":{"S":"branch-001"},"catalogVersion":{"S":"2026-05-20T12:00:00Z"},"priceListId":{"S":"default"}}'