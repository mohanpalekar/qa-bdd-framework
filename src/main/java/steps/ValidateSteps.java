package steps;

import context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import utils.ValueResolver;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class ValidateSteps {

    private final Logger logger = Hooks.getLogger();

    @Then("response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        Response response = ScenarioContext.getResponse();
        int actualStatus = response.getStatusCode();

        logger.info("Asserting response status. Expected: " + expectedStatus + ", Actual: " + actualStatus);

        assertThat(actualStatus)
                .as("Response status code")
                .isEqualTo(expectedStatus);

        logger.info("Status assertion passed ✅");
    }

    @Then("response json should match")
    public void responseJsonShouldMatch(DataTable table) {
        Response response = ScenarioContext.getResponse();
        String responseBody = response.getBody().asString();

        logger.info("🔎 Validating JSON response body: \n" + responseBody);

        for (Map<String, String> row : table.asMaps()) {
            String jsonPath = row.get("key");
            String rawExpected = row.get("value");

            // Resolve dynamic tokens
            String expected = ValueResolver.resolve(rawExpected);
            Object actualObj = response.jsonPath().get(jsonPath);

            // Normalize actual to string for logging but keep object for type checking
            String actualStr = (actualObj == null) ? null : actualObj.toString();

            logger.info("Checking JSON path: " + jsonPath +
                    " | Expected(raw): " + rawExpected +
                    " (resolved: " + expected + ")" +
                    " | Actual: " + actualStr);

            if ("__notEmpty__".equalsIgnoreCase(rawExpected)) {
                assertThat(actualObj)
                        .as("Expected JSON path " + jsonPath + " to be not empty")
                        .isNotNull();
                assertThat(actualStr).isNotEmpty();
                logger.info("✅ JSON path " + jsonPath + " is not empty");
            } else if ("__exists__".equalsIgnoreCase(rawExpected)) {
                assertThat(actualObj)
                        .as("Expected JSON path " + jsonPath + " to exist")
                        .isNotNull();
                logger.info("✅ JSON path " + jsonPath + " exists");
            } else {
                // Smart numeric vs string comparison
                if (actualObj instanceof Number && expected.matches("\\d+(\\.\\d+)?")) {
                    // compare numerically
                    double actualNum = ((Number) actualObj).doubleValue();
                    double expectedNum = Double.parseDouble(expected);
                    assertThat(actualNum)
                            .as("Mismatch at JSON path: " + jsonPath)
                            .isCloseTo(expectedNum, within(0.001)); // <= 0.001 difference allowed
                    logger.info("✅ JSON path " + jsonPath + " numeric match within tolerance");
                    continue;
                } else {
                    // fallback string equality
                    assertThat(actualStr)
                            .as("Mismatch at JSON path: " + jsonPath)
                            .isEqualTo(expected);
                }
                logger.info("✅ JSON path " + jsonPath + " matches expected");
            }
        }
    }


    @Then("response should contain a list of objects")
    public void responseShouldContainAListOfObjects() {
        Response response = ScenarioContext.getResponse();
        List<?> list = response.jsonPath().getList("$");
        logger.info(() -> "Validating response contains a list with size " + list.size());
        assertThat(list).isNotEmpty();
    }
}
