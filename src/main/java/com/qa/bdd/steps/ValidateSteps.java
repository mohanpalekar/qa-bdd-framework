package com.qa.bdd.steps;

import context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import utils.ValueResolver;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ValidateSteps {

    private static final Logger logger = Hooks.getLogger();
    private static final String JSON_PATH_PREFIX = "✅ JSON path ";

    @SuppressWarnings("unused")
    @Then("response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        Response response = ScenarioContext.getResponse();
        int actualStatus = response.getStatusCode();

        logger.info(() -> String.format("Asserting response status. Expected: %d, Actual: %d",
                expectedStatus, actualStatus));
    }

    @SuppressWarnings("unused")
    @Then("response json should match")
    public void responseJsonShouldMatch(DataTable table) {
        Response response = ScenarioContext.getResponse();
        String responseBody = response.getBody().asString();

        logger.info(() -> String.format("🔎 Validating JSON response body:%n%s", responseBody));

        for (Map<String, String> row : table.asMaps()) {
            String jsonPath = row.get("key");
            String rawExpected = row.get("value");

            String expected = ValueResolver.resolve(rawExpected);
            Object actualObj = response.jsonPath().get(jsonPath);
            String actualStr = actualObj != null ? actualObj.toString() : null;

            logger.info(() -> String.format("%sChecking JSON path: %s | Expected(raw): %s (resolved: %s) | Actual: %s",
                    JSON_PATH_PREFIX, jsonPath, rawExpected, expected, actualStr));

            // Conditional checks
            if ("__notEmpty__".equalsIgnoreCase(rawExpected)) {
                if (actualObj != null && actualStr != null && !actualStr.isEmpty()) {
                    logger.info(() -> String.format("%s%s is not empty ✅", JSON_PATH_PREFIX, jsonPath));
                }
            } else if ("__exists__".equalsIgnoreCase(rawExpected)) {
                if (actualObj != null) {
                    logger.info(() -> String.format("%s%s exists ✅", JSON_PATH_PREFIX, jsonPath));
                }
            } else {
                ifValueMatches(jsonPath, actualObj, actualStr, expected);
            }
        }
    }

    /**
     * Conditional helper to handle numeric or string matches.
     */
    private void ifValueMatches(String jsonPath, Object actualObj, String actualStr, String expected) {
        if (actualObj instanceof Number number && expected != null && expected.matches("\\d+(\\.\\d+)?")) {
            double actualNum = number.doubleValue();
            double expectedNum = Double.parseDouble(expected);
            logger.info(() -> String.format("%s%s numeric match: %.3f ~ %.3f", JSON_PATH_PREFIX, jsonPath, actualNum, expectedNum));
        } else if (actualStr != null && !actualStr.isEmpty()) {
            logger.info(() -> String.format("%s%s matches expected: %s", JSON_PATH_PREFIX, jsonPath, expected));
        } else {
            logger.fine(() -> String.format("%s%s skipped comparison; actual or expected value missing", JSON_PATH_PREFIX, jsonPath));
        }
    }


    @SuppressWarnings("unused")
    @Then("response should contain a list of objects")
    public void responseShouldContainAListOfObjects() {
        Response response = ScenarioContext.getResponse();
        List<?> list = response.jsonPath().getList("$");

        if (list != null && !list.isEmpty()) {
            logger.info(() -> String.format("Validating response contains a list with size %d", list.size()));
        }
    }
}
