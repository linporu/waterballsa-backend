.PHONY: fmt lint test test-unit test-integration test-e2e

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
