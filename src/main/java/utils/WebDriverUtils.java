package utils;

import exceptions.ElementNotFoundException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import com.qa.bdd.steps.Hooks;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WebDriverUtils {

    private static final int MAX_ATTEMPTS = 5;

    private WebDriverUtils() {
        // utility
    }

    private static WebElement findElement(String locatorKey, boolean clickable) {
        Logger logger = Hooks.getLogger();
        String raw = LocatorUtils.getRaw(locatorKey);
        logger.log(Level.INFO, "🔍 Resolving locator: {0} → {1}", new Object[]{locatorKey, raw});
        var by = LocatorUtils.get(locatorKey);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                logger.log(Level.FINE, "Attempt {0}/{1} to locate: {2}",
                        new Object[]{attempt, MAX_ATTEMPTS, locatorKey});

                WebElement element = clickable
                        ? WebDriverUtilsInternal.waitForElementClickable(by)
                        : WebDriverUtilsInternal.waitForElement(by);

                if (isUsable(element, locatorKey, logger)) {
                    WebDriverUtilsInternal.scrollToElement(element);
                    WebDriverUtilsInternal.highlightElement(element);
                    sleepSafe(Config.get("ui.actionDelay"));
                    logger.log(Level.INFO, "✅ Found usable element for key: {0} on attempt {1}",
                            new Object[]{locatorKey, attempt});
                    return element;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING,
                        "⚠️ Attempt {0} failed for {1} → By: {2} | Error: {3}",
                        new Object[]{attempt, locatorKey, by, e.getMessage()});
            }
            sleepSafe(Config.get("ui.retryDelay"));
        }

        logger.log(Level.SEVERE, "❌ Could not locate usable element after {0} attempts: {1} → {2}",
                new Object[]{MAX_ATTEMPTS, locatorKey, raw});
        throw new ElementNotFoundException("❌ Could not locate usable element after " + MAX_ATTEMPTS +
                " attempts: " + locatorKey + " → " + raw);
    }

    private static boolean isUsable(WebElement element, String locatorKey, Logger logger) {
        if (element == null) {
            return false;
        }
        if (!element.isDisplayed()) {
            logger.log(Level.WARNING, "⚠️ Element found but not displayed: {0}", locatorKey);
            return false;
        }
        if (!element.isEnabled()) {
            logger.log(Level.WARNING, "⚠️ Element found but not enabled: {0}", locatorKey);
            return false;
        }
        return true;
    }

    private static void sleepSafe(String millisString) {
        try {
            Thread.sleep(Long.parseLong(millisString));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
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
        Hooks.getLogger().log(Level.INFO, "Performing JS click on: {0}", locatorKey);
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript("arguments[0].click();", element);
        WebDriverUtilsInternal.removeHighlight(element);
    }

    public static void jsSendKeys(String locatorKey, String value) {
        WebElement element = findElement(locatorKey, true);
        Hooks.getLogger().log(Level.INFO, "Performing JS sendKeys on: {0} with value: {1}",
                new Object[]{locatorKey, value});
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
            Hooks.getLogger().log(Level.SEVERE, "⚠️ Text mismatch for {0}: expected [{1}] but found [{2}]",
                    new Object[]{locatorKey, expectedText, actualText});
        } else {
            Hooks.getLogger().log(Level.INFO, "✅ Text verified for {0}: {1}",
                    new Object[]{locatorKey, actualText});
        }
    }

}
