package com.qa.bdd.steps;

import context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.testng.Assert;
import utils.ValueResolver;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ValidateSteps {

    private static final Logger logger = Hooks.getLogger();

    private static final String KEY_NOT_EMPTY = "__notempty__";
    private static final String KEY_EXISTS = "__exists__";
    private static final String KEY_NOT_EXISTS = "__not_exists__";
    private static final String ASSERTION_PREFIX = "Expected JSON path '";

    // NEW HELPER METHOD: Extracts nested logic to assert not empty
    private void assertNotEmpty(String jsonPath, Object actual) {
        // Assertion 1: Must exist (not null)
        Assert.assertNotNull(actual, ASSERTION_PREFIX + jsonPath + "' to exist and have a value.");

        // Assertion 2: Uses Pattern Matching switch for clean type checking
        switch (actual) {
            case String actualString:
                Assert.assertFalse(actualString.isEmpty(), ASSERTION_PREFIX + jsonPath + "' to be not empty (String).");
                break;
            case List<?> actualList:
                Assert.assertFalse(actualList.isEmpty(), ASSERTION_PREFIX + jsonPath + "' list to be not empty (List).");
                break;
            case Map<?, ?> actualMap:
                Assert.assertFalse(actualMap.isEmpty(), ASSERTION_PREFIX + jsonPath + "' map to be not empty (Map).");
                break;
            default:
                // For other non-null types (numbers, booleans, etc.), existence is sufficient.
                break;
        }
    }

    //----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    @Then("response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        Response response = ScenarioContext.getResponse();
        int actualStatus = response.getStatusCode();

        logger.info(() -> String.format("Asserting response status. Expected: %d, Actual: %d",
                expectedStatus, actualStatus));
        Assert.assertEquals(actualStatus, expectedStatus, "Response status code mismatch.");
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
            Object actual = response.jsonPath().get(jsonPath);

            logger.info(() -> String.format("Checking JSON path: %s | Expected(raw): %s (resolved: %s) | Actual: %s",
                    jsonPath, rawExpected, expected, actual));

            switch (rawExpected) {
                case KEY_NOT_EMPTY:
                    // Correct logic: Checks for not null and not empty
                    assertNotEmpty(jsonPath, actual);
                    break;

                case KEY_EXISTS:
                    // Correct logic: Checks for existence (must NOT be null)
                    Assert.assertNotNull(actual, ASSERTION_PREFIX + jsonPath + "' to exist, but it was not found (null).");
                    break;

                case KEY_NOT_EXISTS:
                    // Correct logic: Checks for non-existence (must be null)
                    Assert.assertNull(actual, ASSERTION_PREFIX + jsonPath + "' to not exist, but it was found.");
                    break;

                default:
                    // Executes for non-special keywords (e.g., your literal "expected value")
                    Assert.assertNotNull(actual, ASSERTION_PREFIX + jsonPath + "' to exist and have a value, but it was null.");
                    Assert.assertEquals(actual.toString(), expected, "Value mismatch for JSON path '" + jsonPath + "'.");
                    break;
            }
        }
    }

    @SuppressWarnings("unused")
    @Then("response should contain a list of objects")
    public void responseShouldContainAListOfObjects() {
        Response response = ScenarioContext.getResponse();
        List<?> list = response.jsonPath().getList("$");

        Assert.assertNotNull(list, "Expected JSON response to be a list, but it was not.");
        Assert.assertFalse(list.isEmpty(), "Expected JSON response list to be non-empty.");

        logger.info(() -> String.format("Validating response contains a list with size %d", list.size()));
    }
}