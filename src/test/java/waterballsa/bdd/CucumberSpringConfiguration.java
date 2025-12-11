package waterballsa.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Cucumber Spring Boot integration configuration.
 *
 * <p>This class integrates Cucumber with Spring Boot Test and shares the PostgreSQL container setup
 * with existing E2E tests.
 *
 * <p>Uses the singleton container pattern to ensure a single PostgreSQL container is shared across
 * all test classes.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

  static final PostgreSQLContainer<?> postgres;

  // Container lifecycle managed by Testcontainers Ryuk - no manual cleanup needed
  static {
    @SuppressWarnings("resource")
    PostgreSQLContainer<?> container =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("e2e_test")
            .withUsername("test")
            .withPassword("test");
    container.start();
    postgres = container;
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    // Use 'validate' since Liquibase manages the schema (including PostgreSQL ENUM types)
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.liquibase.enabled", () -> "true");
    // Configure HikariCP for test environment
    registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
    registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
  }
}
