package com.qa.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import utils.ValueResolver;
import utils.WebDriverUtils;
import utils.DriverFactory;
import utils.Config;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UiSteps {

    private static final Logger logger = Hooks.getLogger();

    @SuppressWarnings("unused")
    @Given("I launch browser {string}")
    public void launchBrowser(String browserKey) {
        String browser = Config.get(browserKey);
        logger.info(() -> "Launching browser: " + browser);
        DriverFactory.initDriver(browser);
    }

    @SuppressWarnings("unused")
    @Given("I navigate to URL {string}")
    public void navigateToUrl(String urlKey) {
        String url = Config.get(urlKey);
        logger.info(() -> "Navigating to URL: " + url);
        DriverFactory.getDriver().get(url);
    }

    @When("I perform UI actions")
    public void performUIActions(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            String operation = row.get("operation").toLowerCase();
            String locatorKey = row.get("locatorKey");
            String rawValue = row.get("value");
            String value = ValueResolver.resolve(rawValue);

            logger.info(() -> buildLogMessage(operation, locatorKey, value));

            switch (operation) {
                case "type" -> WebDriverUtils.type(locatorKey, value);
                case "click" -> WebDriverUtils.click(locatorKey);
                case "verify text" -> WebDriverUtils.verifyText(locatorKey, value);
                case "mouse over" -> WebDriverUtils.mouseOver(locatorKey);
                case "double click" -> WebDriverUtils.doubleClick(locatorKey);
                case "wait" -> waitForSeconds(value);
                case "keyboard" -> sendKeyboardAction(value);
                case "js click" -> WebDriverUtils.jsClick(locatorKey);
                case "js send keys" -> WebDriverUtils.jsSendKeys(locatorKey, value);
                default -> {
                    String message = "Unknown UI operation: " + operation;
                    logger.severe(message);
                    throw new IllegalArgumentException(message);
                }
            }

            logger.info(() -> "✅ Completed operation: " + operation +
                    (locatorKey != null && !locatorKey.isEmpty() ? " on " + locatorKey : ""));
        }
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

    private void sendKeyboardAction(String action) {
        String keyToSend = String.valueOf(switch (action.toLowerCase()) {
            case "enter" -> Keys.ENTER;
            case "tab" -> Keys.TAB;
            case "ctrl+a" -> Keys.chord(Keys.CONTROL, "a");
            case "ctrl+c" -> Keys.chord(Keys.CONTROL, "c");
            case "ctrl+v" -> Keys.chord(Keys.CONTROL, "v");
            default -> throw new IllegalArgumentException("Unknown keyboard action: " + action);
        });

        new Actions(DriverFactory.getDriver()).sendKeys(keyToSend).perform();
        logger.info(() -> "✅ Sent keyboard action: " + action);
    }

    private String buildLogMessage(String operation, String locatorKey, String value) {
        StringBuilder sb = new StringBuilder("UI Action - Operation: ").append(operation);
        if (locatorKey != null && !locatorKey.isEmpty()) sb.append(", LocatorKey: ").append(locatorKey);
        if (value != null && !value.isEmpty()) sb.append(", Value: ").append(value);
        return sb.toString();
    }
}
