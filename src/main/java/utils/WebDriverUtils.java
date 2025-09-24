package utils;

import exceptions.ElementNotFoundException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import com.qa.bdd.steps.Hooks;
import org.testng.Assert;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WebDriverUtils {

    private static final int MAX_ATTEMPTS = 5;
    private static final Logger logger = Hooks.getLogger();

    private WebDriverUtils() {
        // utility
    }

    private static WebElement findElement(String locatorKey, boolean clickable) {
        String raw = LocatorUtils.getRaw(locatorKey);
        String resolvingMessage = String.format("🔍 Resolving locator: %s → %s", locatorKey, raw);
        logger.info(resolvingMessage);
        var by = LocatorUtils.get(locatorKey);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String attemptMessage = String.format("Attempt %d/%d to locate: %s",
                        attempt, MAX_ATTEMPTS, locatorKey);
                logger.log(Level.FINE, attemptMessage);

                WebElement element = clickable
                        ? WebDriverUtilsInternal.waitForElementClickable(by)
                        : WebDriverUtilsInternal.waitForElement(by);

                if (isUsable(element, locatorKey)) {
                    WebDriverUtilsInternal.scrollToElement(element);
                    WebDriverUtilsInternal.highlightElement(element);
                    sleepSafe(Config.get("ui.actionDelay"));
                    String foundMessage = String.format("✅ Found usable element for key: %s on attempt %d",
                            locatorKey, attempt);
                    logger.info(foundMessage);
                    return element;
                }
            } catch (Exception e) {
                String warningMessage = String.format("⚠️ Attempt %d failed for %s → By: %s | Error: %s",
                        attempt, locatorKey, by, e.getMessage());
                logger.log(Level.WARNING, warningMessage);
            }
            sleepSafe(Config.get("ui.retryDelay"));
        }

        String failMessage = String.format("❌ Could not locate usable element after %d attempts: %s → %s",
                MAX_ATTEMPTS, locatorKey, raw);
        logger.log(Level.SEVERE, failMessage);
        throw new ElementNotFoundException(failMessage);
    }

    private static boolean isUsable(WebElement element, String locatorKey) {
        if (element == null) {
            return false;
        }
        if (!element.isDisplayed()) {
            String notDisplayedMessage = String.format("⚠️ Element found but not displayed: %s", locatorKey);
            logger.log(Level.WARNING, notDisplayedMessage);
            return false;
        }
        if (!element.isEnabled()) {
            String notEnabledMessage = String.format("⚠️ Element found but not enabled: %s", locatorKey);
            logger.log(Level.WARNING, notEnabledMessage);
            return false;
        }
        return true;
    }

    private static void sleepSafe(String millisString) {
        try {
            Thread.sleep(Long.parseLong(millisString));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            String interruptedMessage = String.format("Sleep interrupted: %s", ie.getMessage());
            logger.log(Level.WARNING, interruptedMessage);
        }
    }

    // ==== Public Actions ====

    public static void type(String locatorKey, String value) {
        WebElement element = findElement(locatorKey, true);
        element.clear();
        element.sendKeys(value);
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void click(String locatorKey) {
        WebElement element = findElement(locatorKey, true);
        element.click();
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void jsClick(String locatorKey) {
        WebElement element = findElement(locatorKey, true);
        String clickMessage = String.format("Performing JS click on: %s", locatorKey);
        logger.log(Level.INFO, clickMessage);
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript("arguments[0].click();", element);
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void jsSendKeys(String locatorKey, String value) {
        WebElement element = findElement(locatorKey, true);
        String sendKeysMessage = String.format("Performing JS sendKeys on: %s with value: %s",
                locatorKey, value);
        logger.log(Level.INFO, sendKeysMessage);
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript("arguments[0].value = arguments[1];", element, value);
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void mouseOver(String locatorKey) {
        WebElement element = findElement(locatorKey, false);
        WebDriverUtilsInternal.mouseOver(element);
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void doubleClick(String locatorKey) {
        WebElement element = findElement(locatorKey, true);
        WebDriverUtilsInternal.doubleClick(element);
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void verifyText(String locatorKey, String expectedText) {
        WebElement element = findElement(locatorKey, false);
        String actualText = element.getText();
        WebDriverUtilsInternal.removeHighlight(element);

        Assert.assertEquals(actualText, expectedText,
                String.format("Text mismatch for element '%s'. Expected: '%s', Actual: '%s'",
                        locatorKey, expectedText, actualText));

        String successMessage = String.format("✅ Text verified for %s: %s",
                locatorKey, actualText);
        logger.log(Level.INFO, successMessage);
    }
}