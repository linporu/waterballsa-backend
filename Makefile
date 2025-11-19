.PHONY: fmt lint test test-unit test-integration test-e2e migrate-up migrate-down migrate-drop migrate-status migrate-create

fmt:
	./mvnw spotless:apply

lint:
	./mvnw checkstyle:check

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
	docker compose exec db psql -U admin -d waterballsa -c "DROP TYPE IF EXISTS user_role CASCADE" || true

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
