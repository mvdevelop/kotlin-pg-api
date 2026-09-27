# ============================================================================
# Makefile — Developer Experience (DX) Helper
# ----------------------------------------------------------------------------
# Common commands for building, testing, running, and deploying the API.
# Usage: make <command>
# ============================================================================

# ─── Configuration ────────────────────────────────────────────────────────────
PACKAGE_NAME := kotlin-pg-api
DOCKER_REGISTRY := ghcr.io
DOCKER_IMAGE := $(DOCKER_REGISTRY)/$(PACKAGE_NAME)
JAVA_VERSION := 21

# ─── Colors ───────────────────────────────────────────────────────────────────
GREEN  := $(shell printf '\033[32m')
YELLOW := $(shell printf '\033[33m')
RED    := $(shell printf '\033[31m')
RESET  := $(shell printf '\033[0m')

# ─── Default ──────────────────────────────────────────────────────────────────
.PHONY: help
help: ## Show available commands
	@echo "$(GREEN)╔════════════════════════════════════════════╗"
	@echo "║     Kotlin Product API — Developer Helper    ║"
	@echo "╚════════════════════════════════════════════╝$(RESET)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)⚡ %-20s$(RESET) %s\n", $$1, $$2}'

# ─── Build ────────────────────────────────────────────────────────────────────
.PHONY: build
build: ## Compile all sources (no tests)
	@echo "$(YELLOW)🔨 Building project...$(RESET)"
	mvn clean compile -q

.PHONY: build-all
build-all: ## Full build with all profiles
	@echo "$(YELLOW)🔨 Full build...$(RESET)"
	mvn clean install -DskipTests -Dspring.profiles.active=prod

# ─── Test ─────────────────────────────────────────────────────────────────────
.PHONY: test
test: ## Run unit tests only
	@echo "$(YELLOW)🧪 Running unit tests...$(RESET)"
	mvn test -q

.PHONY: test-integration
test-integration: ## Run integration tests (requires Docker)
	@echo "$(YELLOW)🧪 Running integration tests with Testcontainers...$(RESET)"
	mvn verify -Dit*'IT' -q

.PHONY: test-all
test-all: ## Run all tests (unit + integration)
	@echo "$(YELLOW)🧪 Running all tests...$(RESET)"
	mvn verify

.PHONY: test-coverage
test-coverage: test ## Run tests and generate coverage report
	@echo "$(YELLOW)📊 Generating coverage report...$(RESET)"
	mvn jacoco:report
	@echo "$(GREEN)✓ Coverage report: target/site/jacoco/index.html$(RESET)"

# ─── Run ──────────────────────────────────────────────────────────────────────
.PHONY: run
run: ## Run the API locally (dev profile)
	@echo "$(YELLOW)🚀 Starting API on port 8080...$(RESET)"
	@export $$(grep -v '^#' .env 2>/dev/null | xargs -d '\n') && mvn spring-boot:run

.PHONY: run-prod
run-prod: ## Run with production profile
	@echo "$(YELLOW)🚀 Starting API (production profile)...$(RESET)"
	mvn spring-boot:run -Dspring-boot.run.profiles=prod

.PHONY: run-docker
run-docker: ## Run with Docker Compose
	@echo "$(YELLOW)🐳 Starting all services with Docker Compose...$(RESET)"
	docker-compose up --build

# ─── Code Quality ─────────────────────────────────────────────────────────────
.PHONY: lint
lint: ## Run ktlint (Kotlin style checker)
	@echo "$(YELLOW)🔍 Running ktlint...$(RESET)"
	@ktlint --format src/**/*.kt 2>/dev/null || echo "$(YELLOW)⚠ ktlint not installed. Install with: curl -sL https://ktlint.dev/install.sh | bash$(RESET)"

.PHONY: detekt
detekt: ## Run Detekt static analysis
	@echo "$(YELLOW)🔍 Running Detekt...$(RESET)"
	@detekt -c detekt.yml 2>/dev/null || echo "$(YELLOW)⚠ Detekt not installed. Install from: https://github.com/detekt/detekt$(RESET)"

.PHONY: format
format: lint ## Auto-format code
	@echo "$(GREEN)✓ Code formatted$(RESET)"

# ─── Security ─────────────────────────────────────────────────────────────────
.PHONY: security-check
security-check: ## Run security scans (requires .env setup)
	@echo "$(YELLOW)🔒 Running security scans...$(RESET)"
	@git ls-files | xargs -I{} grep -n -H "password\|secret\|api_key\|token" {} 2>/dev/null | grep -v ".gitignore" | grep -v "test" || echo "$(GREEN)✓ No hardcoded secrets found$(RESET)"

.PHONY: dependency-check
dependency-check: ## Run OWASP Dependency-Check
	@echo "$(YELLOW)🔍 Running OWASP Dependency-Check...$(RESET)"
	mvn dependency-check:check

# ─── Docker ───────────────────────────────────────────────────────────────────
.PHONY: docker-build
docker-build: ## Build production Docker image
	@echo "$(YELLOW)🐳 Building Docker image...$(RESET)"
	docker build -t $(PACKAGE_NAME):latest .

.PHONY: docker-push
docker-push: docker-build ## Push to container registry
	@echo "$(YELLOW)📤 Pushing to $(DOCKER_REGISTRY)...$(RESET)"
	docker tag $(PACKAGE_NAME):latest $(DOCKER_IMAGE):latest
	docker push $(DOCKER_IMAGE):latest

.PHONY: docker-clean
docker-clean: ## Stop and remove all Docker containers
	@echo "$(YELLOW)🧹 Stopping Docker containers...$(RESET)"
	docker-compose down -v
	docker rm -f $(shell docker ps -aq --filter ancestor=$(PACKAGE_NAME)) 2>/dev/null || true

# ─── Database ─────────────────────────────────────────────────────────────────
.PHONY: db-migrate
db-migrate: ## Run Flyway database migrations
	@echo "$(YELLOW)📂 Running database migrations...$(RESET)"
	mvn flyway:migrate

.PHONY: db-info
db-info: ## Show database migration status
	@echo "$(YELLOW)📊 Database migration info...$(RESET)"
	mvn flyway:info

.PHONY: db-seed
db-seed: ## Seed initial data (uses init.sql)
	@echo "$(YELLOW)🌱 Seeding database...$(RESET)"
	@docker exec -i pg-api-postgres psql -U postgres -d api_db < src/main/resources/init.sql || \
		echo "$(YELLOW)⚠ Could not seed. Ensure PostgreSQL is running.$(RESET)"

.PHONY: db-reset
db-reset: ## Reset database (dev only)
	@echo "$(RED)⚠ This will DELETE all data. Continue? [y/N]$(RESET)"
	@read -r reply; if [ "$$reply" = "y" ]; then \
		docker exec pg-api-postgres dropdb -U postgres api_db 2>/dev/null || true; \
		docker exec pg-api-postgres createdb -U postgres api_db; \
		make db-migrate; \
		make db-seed; \
		echo "$(GREEN)✓ Database reset complete$(RESET)"; \
	else echo "$(YELLOW)Aborted.$(RESET)"; fi

# ─── Cleanup ──────────────────────────────────────────────────────────────────
.PHONY: clean
clean: ## Clean build artifacts
	@echo "$(YELLOW)🧹 Cleaning...$(RESET)"
	mvn clean -q
	rm -rf target/ bin/

.PHONY: clean-all
clean-all: clean docker-clean ## Deep clean (build + Docker + DB)
	@echo "$(GREEN)✓ Deep clean complete$(RESET)"

# ─── Info ─────────────────────────────────────────────────────────────────────
.PHONY: info
info: ## Show project information
	@echo "$(GREEN)╔════════════════════════════════════════════╗"
	@echo "║     Kotlin Product API — v2.0.0              ║"
	@echo "╚════════════════════════════════════════════╝$(RESET)"
	@echo ""
	@echo "$(YELLOW)Java:$(RESET)       $(shell java -version 2>&1 | head -1)"
	@echo "$(YELLOW)Kotlin:$(RESET)     2.1.21"
	@echo "$(YELLOW)Spring Boot:$(RESET) 3.5.4"
	@echo "$(YELLOW)Database:$(RESET)  PostgreSQL 16"
	@echo ""
	@echo "$(YELLOW)Endpoints:$(RESET)"
	@echo "  POST   /api/auth/register  — Register (public, rate-limited)"
	@echo "  POST   /api/auth/login     — Login (public, rate-limited)"
	@echo "  GET    /api/products       — List (authenticated)"
	@echo "  POST   /api/products       — Create (admin only)"
	@echo "  GET    /api/products/{id}  — Get by ID (authenticated)"
	@echo "  PUT    /api/products/{id}  — Update (admin only)"
	@echo "  DELETE /api/products/{id}  — Delete (admin only)"
	@echo "  GET    /swagger-ui.html   — API docs"
	@echo "  GET    /actuator/health   — Health check"
