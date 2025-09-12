package com.qa.bdd.steps;

import api.ApiYamlSpec;
import api.ApiSpecLoader;
import api.PayloadOverwriter;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import context.ScenarioContext;
import utils.Config;
import utils.ValueResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ApiSteps {

    private final Logger logger = Hooks.getLogger();

    @SuppressWarnings("unused")
    @When("I call api spec {string} without overrides")
    public void iCallApiWithoutOverrides(String yamlFile) {
        iCallApiWithOverrides(yamlFile, null);
    }

    @When("I call api spec {string} with overrides")
    public void iCallApiWithOverrides(String yamlFile, DataTable table) {
        logger.info(() -> "Loading API spec: " + yamlFile);
        ApiYamlSpec spec = ApiSpecLoader.load(yamlFile);

        Map<String, String> pathParams = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();
        Map<String, String> jsonOverrides = new HashMap<>();

        if (table != null && !table.isEmpty()) {
            table.asMaps(String.class, String.class).forEach(map -> {
                String key = map.get("key");
                String rawValue = map.get("value");
                String value = ValueResolver.resolve(rawValue);

                if ("wait".equalsIgnoreCase(key)) {
                    waitForSeconds(value);
                    return;
                }

                if (key.startsWith("path:")) {
                    pathParams.put(key.substring(5), value);
                } else if (key.startsWith("query:")) {
                    queryParams.computeIfAbsent(key.substring(6), k -> new ArrayList<>()).add(value);
                } else {
                    jsonOverrides.put(key, value);
                }

                logger.info(() -> "Override -> key: " + key + " | value: " + value);
            });
        }

        String payload = (spec.getPayload() != null && !spec.getPayload().isBlank())
                ? PayloadOverwriter.buildPayload(spec.getPayload(), jsonOverrides)
                : null;

        logger.info(() -> "Calling API " + spec.getMethod() + " " + spec.getUri());
        logger.info(() -> "Path params: " + pathParams);
        logger.info(() -> "Query params: " + queryParams);
        if (payload != null) logger.info(() -> "Request body: " + payload);

        var request = RestAssured.given()
                .baseUri(Config.get("baseUri"))
                .headers(spec.getHeaders())
                .pathParams(pathParams);

        queryParams.forEach((k, v) -> request.queryParam(k, v.toArray()));
        if (payload != null) request.body(payload);

        Response response = request.request(spec.getMethod(), spec.getUri());

        logger.info(() -> "Response status: " + response.getStatusCode());
        logger.info(() -> "Response body: " + response.getBody().asPrettyString());

        ScenarioContext.saveResponse(response);
    }

    private void waitForSeconds(String value) {
        int seconds = Integer.parseInt(value);
        logger.info(() -> "⏳ Waiting for " + seconds + " seconds...");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info(() -> "✅ Waited for " + seconds + " seconds");
    }
}
