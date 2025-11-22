.PHONY: fmt lint build test test-unit test-integration test-e2e migrate-up migrate-down migrate-drop migrate-refresh migrate-status migrate-create db-backup db-restore

fmt:
	./mvnw spotless:apply

lint:
	./mvnw checkstyle:check

build:
	./mvnw clean compile

test:
	./mvnw test

test-unit:
	./mvnw test -Dtest="waterballsa.unit.**"

test-integration:
	./mvnw test -Dtest="waterballsa.integration.**"

test-e2e:
	./mvnw test -Dtest="waterballsa.e2e.**"

# Database Migration Commands (runs inside Docker container with .env config)
migrate-up:
	docker compose exec backend mvn liquibase:update

# Rollback = 1
migrate-down:
	docker compose exec backend mvn liquibase:rollback -Dliquibase.rollbackCount=1

migrate-drop:
	docker compose exec backend mvn liquibase:dropAll
	@echo "Cleaning up remaining database types..."
	docker compose exec -T db psql -U admin -d waterballsa < src/main/resources/db/drop-db.sql || true

migrate-refresh:
	@echo "Refreshing database: drop all and re-run migrations..."
	@$(MAKE) migrate-drop
	@$(MAKE) migrate-up

migrate-status:
	docker compose exec backend mvn liquibase:status

migrate-create:
	@read -p "Enter migration name (e.g., add-user-email-column): " name; \
	timestamp=$$(date +%Y%m%d%H%M%S); \
	filename="src/main/resources/db/changelog/migrations/$${timestamp}-$${name}.sql"; \
	echo "Creating migration file: $${filename}"; \
	echo "--liquibase formatted sql" > $${filename}; \
	echo "" >> $${filename}; \
	echo "--changeset liquibase:$${timestamp}-$${name}" >> $${filename}; \
	echo "--comment: TODO: Add description here" >> $${filename}; \
	echo "" >> $${filename}; \
	echo "-- TODO: Add your SQL here" >> $${filename}; \
	echo "" >> $${filename}; \
	echo "--rollback TODO: Add rollback SQL here" >> $${filename}; \
	echo "Migration file created! Don't forget to add it to db.changelog-master.yaml"

# Database Backup & Restore Commands (operates on Docker container database)
db-backup:
	@mkdir -p backups
	@timestamp=$$(date +%Y%m%d_%H%M%S); \
	filename="backups/waterballsa_$${timestamp}.sql"; \
	echo "Backing up database from Docker container to $${filename}..."; \
	docker compose exec -T db pg_dump -U admin -d waterballsa --clean --if-exists > $${filename}; \
	echo "Database backup completed: $${filename}"

db-restore:
	@if [ -z "$(FILE)" ]; then \
		echo "Error: Please specify backup file with FILE=path/to/backup.sql"; \
		echo "Example: make db-restore FILE=backups/waterballsa_20250120_123456.sql"; \
		exit 1; \
	fi; \
	if [ ! -f "$(FILE)" ]; then \
		echo "Error: Backup file $(FILE) not found"; \
		exit 1; \
	fi; \
	echo "Restoring database from $(FILE) to Docker container..."; \
	echo "Warning: This will drop all existing data in the container database!"; \
	read -p "Are you sure you want to continue? (yes/no): " confirm; \
	if [ "$$confirm" = "yes" ]; then \
		docker compose exec -T db psql -U admin -d waterballsa < $(FILE); \
		echo "Database restore completed from $(FILE)"; \
	else \
		echo "Database restore cancelled"; \
	fi
