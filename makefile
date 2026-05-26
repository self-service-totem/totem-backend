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

sam-build: openapi-bundle
	sam build


# *****Seccion dedicada a git *****#
GH_CONFIG_DIR_PATH ?= $(HOME)/.config/gh-ffresco
PROJECT_OWNER ?= self-service-totem
PROJECT_NUMBER ?= 1

# Shows current gh authentication status using the project-specific config.
gh-auth-status:
	GH_CONFIG_DIR="$(GH_CONFIG_DIR_PATH)" gh auth status

# Login gh using the project-specific config.
gh-auth-login:
	GH_CONFIG_DIR="$(GH_CONFIG_DIR_PATH)" gh auth login

# Refresh gh auth adding GitHub Projects scope.
gh-auth-refresh-project:
	GH_CONFIG_DIR="$(GH_CONFIG_DIR_PATH)" gh auth refresh -s project

# Lists GitHub Projects for the configured owner.
gh-project-list:
	GH_CONFIG_DIR="$(GH_CONFIG_DIR_PATH)" gh project list --owner "$(PROJECT_OWNER)"

# Creates a draft item directly in a GitHub Project.
# Title is derived from BODY_FILE name.
# Body is the content of BODY_FILE.
GH_CONFIG_DIR_PATH ?= $(HOME)/.config/gh-ffresco
PROJECT_OWNER ?= self-service-totem
PROJECT_NUMBER ?= 1
REPO ?= self-service-totem/totem-backend

# Creates a real GitHub issue from a markdown file and adds it to the GitHub Project.
# Title is derived from BODY_FILE name.
# Body is the content of BODY_FILE.
gh-ticket-create:
	@if [ -z "$(BODY_FILE)" ]; then echo "Missing BODY_FILE. Usage: make gh-ticket-create BODY_FILE=<file>"; exit 1; fi
	@if [ ! -f "$(BODY_FILE)" ]; then echo "BODY_FILE not found: $(BODY_FILE)"; exit 1; fi
	@TITLE=$$(basename "$(BODY_FILE)" .md | sed -E 's/^([A-Za-z]+)-([0-9]+)-/\1-\2 /; s/-/ /g'); \
	echo "Creating GitHub Issue:"; \
	echo "  Repo: $(REPO)"; \
	echo "  Title: $$TITLE"; \
	echo "  Body file: $(BODY_FILE)"; \
	ISSUE_URL=$$(GH_CONFIG_DIR="$(GH_CONFIG_DIR_PATH)" gh issue create \
		--repo "$(REPO)" \
		--title "$$TITLE" \
		--body-file "$(BODY_FILE)"); \
	echo "Issue created: $$ISSUE_URL"; \
	ISSUE_NUMBER=$$(basename "$$ISSUE_URL"); \
	echo "$$ISSUE_NUMBER" > docs/tickets/generated/last-ticket-number.txt; \
	echo "$$ISSUE_URL" > docs/tickets/generated/last-ticket-url.txt; \
	echo "Ticket number: $$ISSUE_NUMBER"; \
	echo "Ticket URL saved to docs/tickets/generated/last-ticket-url.txt"; \
	echo "Adding issue to GitHub Project:"; \
	GH_CONFIG_DIR="$(GH_CONFIG_DIR_PATH)" gh project item-add "$(PROJECT_NUMBER)" \
		--owner "$(PROJECT_OWNER)" \
		--url "$$ISSUE_URL"; \
	echo "Added to Project: https://github.com/orgs/$(PROJECT_OWNER)/projects/$(PROJECT_NUMBER)"; \
	echo ""; \
	echo "Suggested branch:"; \
	echo "git checkout -b  feature/$$ISSUE_NUMBER-$$(basename "$(BODY_FILE)" .md | sed -E 's/^([A-Za-z]+)-([0-9]+)-//')"

gh-ticket-create-help:
	@echo "Creates a real GitHub issue and adds it to the GitHub Project."
	@echo ""
	@echo "First time setup:"
	@echo "  make gh-auth-login"
	@echo "  make gh-auth-refresh-project"
	@echo "  make gh-project-list"
	@echo ""
	@echo "Example:"
	@echo "  make gh-ticket-create BODY_FILE=docs/tickets/generated/public-menu.md"

# Creates a GitHub PR.
# Creates a GitHub PR from the current branch.
# Base is always develop.
# Head is the current git branch.
# Title is derived from the branch last part.
# Body file is derived from the branch last part.
#
# Example:
# Current branch: feature/1-public-menu
#
# Uses:
#   base:  develop
#   head:  feature/1-public-menu
#   title: 1 public menu
#   body:  docs/tickets/generated/1-public-menu.md

gh-pr-create:
	@HEAD=$$(git rev-parse --abbrev-ref HEAD); \
	if [ "$$HEAD" = "HEAD" ]; then echo "You are in detached HEAD state"; exit 1; fi; \
	if [ "$$HEAD" = "main" ] || [ "$$HEAD" = "develop" ]; then echo "Refusing to create PR from protected branch: $$HEAD"; exit 1; fi; \
	BRANCH_NAME=$$(basename "$$HEAD"); \
	BODY_FILE="docs/tickets/generated/$$BRANCH_NAME.md"; \
	TITLE=$$(echo "$$BRANCH_NAME" | sed -E 's/-/ /g'); \
	if [ ! -f "$$BODY_FILE" ]; then echo "BODY_FILE not found: $$BODY_FILE"; exit 1; fi; \
	echo "Creating GitHub PR:"; \
	echo "  Base: develop"; \
	echo "  Head: $$HEAD"; \
	echo "  Title: $$TITLE"; \
	echo "  Body file: $$BODY_FILE"; \
	GH_CONFIG_DIR="$(GH_CONFIG_DIR_PATH)" gh pr create \
		--base develop \
		--head "$$HEAD" \
		--title "$$TITLE" \
		--body-file "$$BODY_FILE"

gh-pr-create-help:
	@echo "Creates a GitHub pull request from the current branch."
	@echo ""
	@echo "Rules:"
	@echo "  Base branch is always: develop"
	@echo "  Head branch is detected from the current git branch"
	@echo "  Title is generated from the branch last part"
	@echo "  Body file is generated from the branch last part"
	@echo ""
	@echo "Expected branch pattern:"
	@echo "  feature/1-public-menu"
	@echo ""
	@echo "Expected body file:"
	@echo "  docs/tickets/generated/1-public-menu.md"
	@echo ""
	@echo "Usage:"
	@echo "  make gh-pr-create"
	@echo ""
	@echo "Example:"
	@echo "  git checkout feature/1-public-menu"
	@echo "  make gh-pr-create"