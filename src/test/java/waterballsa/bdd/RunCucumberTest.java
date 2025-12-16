package waterballsa.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit Platform Suite for running Cucumber BDD tests.
 *
 * <p>This class serves as the entry point for executing Cucumber feature files through Maven
 * Surefire. It can be targeted specifically using:
 *
 * <pre>
 * ./mvnw test -Dtest=RunCucumberTest
 * make test-bdd
 * </pre>
 *
 * <p>Features can be filtered by tags using:
 *
 * <pre>
 * ./mvnw test -Dtest=RunCucumberTest -Dcucumber.filter.tags="@isa"
 * make test-bdd-isa
 * </pre>
 *
 * @see <a href="https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-junit-platform-engine">
 *     Cucumber JUnit Platform Engine</a>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "waterballsa.bdd")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value =
        "pretty,html:target/cucumber-reports/cucumber.html,json:target/cucumber-reports/cucumber.json")
public class RunCucumberTest {
  // This class is intentionally empty - JUnit Platform will discover and run
  // Cucumber scenarios based on the annotations above
}
