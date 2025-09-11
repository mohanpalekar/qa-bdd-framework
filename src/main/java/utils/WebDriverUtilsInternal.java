package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WebDriverUtilsInternal {
    private static final int DEFAULT_TIMEOUT = 10;

    private static WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    private static WebDriverWait getWait(int seconds) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(seconds));
    }

    public static WebElement waitForElement(By locator) {
        return getWait(DEFAULT_TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForElementClickable(By locator) {
        return getWait(DEFAULT_TIMEOUT).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static void scrollToElement(WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public static void highlightElement(WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].style.border='3px solid red'", element);
    }

    public static void removeHighlight(WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].style.border=''", element);
    }

    public static void mouseOver(WebElement element) {
        new org.openqa.selenium.interactions.Actions(getDriver()).moveToElement(element).perform();
    }

    public static void doubleClick(WebElement element) {
        new org.openqa.selenium.interactions.Actions(getDriver()).doubleClick(element).perform();
    }
}
