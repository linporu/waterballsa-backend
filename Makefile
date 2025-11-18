.PHONY: fmt lint test

fmt:
	./mvnw spotless:apply

lint:
	./mvnw checkstyle:check

test:
	./mvnw test
