package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import steps.Hooks;

import java.util.Map;
import java.util.logging.Logger;

public class WebDriverUtils {

    /**
     * Robust element finder:
     * - 5 attempts with small delay
     * - checks displayed & enabled
     * - logs instead of throwing
     */
    private static WebElement findElement(String locatorKey, boolean clickable) {
        Logger logger = Hooks.getLogger();
        String raw = LocatorUtils.getRaw(locatorKey);
        logger.info("🔍 Resolving locator: " + locatorKey + " → " + raw);
        var by = LocatorUtils.get(locatorKey);
        WebElement element = null;
        int maxAttempts = 5;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                logger.fine("Attempt " + attempt + "/" + maxAttempts + " to locate: " + locatorKey);

                element = clickable
                        ? WebDriverUtilsInternal.waitForElementClickable(by)
                        : WebDriverUtilsInternal.waitForElement(by);

                if (element != null) {
                    // additional safety checks
                    if (!element.isDisplayed()) {
                        logger.warning("⚠️ Element found but not displayed: " + locatorKey);
                        element = null;
                    } else if (!element.isEnabled()) {
                        logger.warning("⚠️ Element found but not enabled: " + locatorKey);
                        element = null;
                    } else {
                        WebDriverUtilsInternal.scrollToElement(element);
                        WebDriverUtilsInternal.highlightElement(element);

                        // pause after highlight
                        long delay = Long.parseLong(Config.get("ui.actionDelay"));
                        Thread.sleep(delay);

                        logger.info("✅ Found usable element for key: " + locatorKey + " on attempt " + attempt);
                        return element;
                    }
                }
            } catch (Exception e) {
                logger.warning("⚠️ Attempt " + attempt + " failed for " + locatorKey +
                        " → By: " + by + " | Error: " + e.getMessage());
            }

            // wait a bit before retry
            try {
                Thread.sleep(Long.parseLong(Config.get("ui.retryDelay")));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        logger.severe("❌ Could not locate usable element after " + maxAttempts + " attempts: " + locatorKey + " → " + raw);
        throw new RuntimeException("❌ Could not locate usable element after " + maxAttempts + " attempts: " + locatorKey + " → " + raw);
    }

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
        Hooks.getLogger().info("Performing JS click on: " + locatorKey);
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript("arguments[0].click();", element);
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void jsSendKeys(String locatorKey, String value) {
        WebElement element = findElement(locatorKey, true);
        Hooks.getLogger().info("Performing JS sendKeys on: " + locatorKey + " with value: " + value);
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
        if (!actualText.equals(expectedText)) {
            Hooks.getLogger().severe("⚠️ Text mismatch for " + locatorKey +
                    ": expected [" + expectedText + "] but found [" + actualText + "]");
        } else {
            Hooks.getLogger().info("✅ Text verified for " + locatorKey + ": " + actualText);
        }
    }

    // --- Batch operations ---
    public static void typeMultiple(Map<String, String> locatorValueMap) {
        locatorValueMap.forEach(WebDriverUtils::type);
    }

    public static void clickMultiple(Iterable<String> locatorKeys) {
        locatorKeys.forEach(WebDriverUtils::click);
    }

    public static void mouseOverMultiple(Iterable<String> locatorKeys) {
        locatorKeys.forEach(WebDriverUtils::mouseOver);
    }

    public static void doubleClickMultiple(Iterable<String> locatorKeys) {
        locatorKeys.forEach(WebDriverUtils::doubleClick);
    }

    public static void verifyTextMultiple(Map<String, String> locatorExpectedMap) {
        locatorExpectedMap.forEach(WebDriverUtils::verifyText);
    }
}
