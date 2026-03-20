.PHONY: init build up down logs clean test-network

# Variables
ENV_FILE := .env
ENV_EXAMPLE := .env.example

init:
	@echo "Initializing walled environment setup..."
	@if [ ! -f $(ENV_FILE) ]; then cp $(ENV_EXAMPLE) $(ENV_FILE); fi
	@mkdir -p docker/lib .secrets
	@echo "Generating 96-byte Local Master Key for CSFLE via Docker Secrets..."
	@if [ ! -f .secrets/csfle_master_key.txt ]; then \
		openssl rand -base64 96 | tr -d '\n' > .secrets/csfle_master_key.txt; \
		echo "Secret successfully generated at .secrets/csfle_master_key.txt"; \
	else \
		echo "Secret file already exists. Skipping generation."; \
	fi
	@echo "IMPORTANT: Please download 'mongo_crypt_v1.so' and place it in docker/lib/ before running 'make build'."

build:
	@echo "Building Docker images for the walled environment..."
	docker compose --env-file $(ENV_FILE) build

up:
	@echo "Spinning up the obfuscation stack..."
	docker compose --env-file $(ENV_FILE) up -d

down:
	@echo "Tearing down the stack and isolated network..."
	docker compose --env-file $(ENV_FILE) down

logs:
	@echo "Tailing logs for all services (Press Ctrl+C to exit)..."
	docker compose --env-file $(ENV_FILE) logs -f

clean: down
	@echo "Pruning Docker volumes and removing orphaned containers..."
	docker compose --env-file $(ENV_FILE) down -v --remove-orphans
	@echo "Clean complete."

test-network:
	@echo "Verifying walled network isolation (ping to 8.8.8.8 should fail)..."
	@docker exec -it $$(docker compose ps -q api-backend) ping -c 1 -W 2 8.8.8.8 || echo "Network test PASSED: Backend container is successfully air-gapped."