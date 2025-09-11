package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility to fetch By locators (normal & relative).
 *
 * Examples in locators.properties:
 *   login.button=css:.login-btn
 *   password.input=id:passwordField
 *   login.submit=relative:tag:button|below=password.input|toRightOf=remember.checkbox|near=hint.text:80
 */
public final class LocatorUtils {
    private static final Properties locators = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("src/test/resources/config/locators.properties")) {
            locators.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load locators.properties", e);
        }
    }

    public static By get(String key) {
        String raw = locators.getProperty(key);
        if (raw == null) {
            throw new RuntimeException("Locator not found for key: " + key);
        }

        if (raw.startsWith("relative:")) {
            return buildRelativeBy(raw, key);
        }
        return parseSimple(raw, key);
    }

    public static String getRaw(String key) {
        return locators.getProperty(key);
    }

    private static By parseSimple(String value, String key) {
        String[] parts = value.split(":", 2);
        if (parts.length != 2) {
            throw new RuntimeException("Invalid locator format for key: " + key + " -> " + value);
        }

        String type = parts[0].trim().toLowerCase();
        String locator = parts[1].trim();

        return switch (type) {
            case "css" -> By.cssSelector(locator);
            case "xpath" -> By.xpath(locator);
            case "id" -> By.id(locator);
            case "name" -> By.name(locator);
            case "classname" -> By.className(locator);
            case "tagname" -> By.tagName(locator);
            case "linktext" -> By.linkText(locator);
            case "partiallinktext" -> By.partialLinkText(locator);
            default -> throw new RuntimeException("Unsupported locator type: " + type + " for key: " + key);
        };
    }

    private static By buildRelativeBy(String raw, String key) {
        // relative:tag:button|below=password.input|toRightOf=remember.checkbox|near=hint.text:80
        String[] parts = raw.split("\\|");

        By baseBy = parseBase(parts[0]);
        var relBy = RelativeLocator.with(baseBy);

        for (int i = 1; i < parts.length; i++) {
            String condition = parts[i].trim();
            String lower = condition.toLowerCase();

            if (lower.startsWith("below=")) {
                String refKey = condition.split("=")[1].trim();
                WebElement refEl = DriverFactory.getDriver().findElement(get(refKey));
                relBy = relBy.below(refEl);

            } else if (lower.startsWith("above=")) {
                String refKey = condition.split("=")[1].trim();
                WebElement refEl = DriverFactory.getDriver().findElement(get(refKey));
                relBy = relBy.above(refEl);

            } else if (lower.startsWith("toleftof=")) {
                String refKey = condition.split("=")[1].trim();
                WebElement refEl = DriverFactory.getDriver().findElement(get(refKey));
                relBy = relBy.toLeftOf(refEl);

            } else if (lower.startsWith("torightof=")) {
                String refKey = condition.split("=")[1].trim();
                WebElement refEl = DriverFactory.getDriver().findElement(get(refKey));
                relBy = relBy.toRightOf(refEl);

            } else if (lower.startsWith("near=")) {
                String[] nearParts = condition.split("=|:");
                String refKey = nearParts[1].trim();
                int distance = nearParts.length > 2 ? Integer.parseInt(nearParts[2]) : 50;
                WebElement refEl = DriverFactory.getDriver().findElement(get(refKey));
                relBy = relBy.near(refEl, distance);

            } else {
                throw new RuntimeException("Unknown relative condition: " + condition + " in key: " + key);
            }
        }
        return relBy;
    }

    private static By parseBase(String base) {
        // e.g. relative:tag:button or relative:css:.class
        String[] tokens = base.split(":");
        if (tokens.length < 3) {
            throw new RuntimeException("Invalid relative base: " + base);
        }
        String type = tokens[1].trim().toLowerCase();
        String value = tokens[2].trim();

        return switch (type) {
            case "id" -> By.id(value);
            case "css" -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            case "tag" -> By.tagName(value);
            default -> throw new RuntimeException("Unsupported relative base type: " + type);
        };
    }

    private LocatorUtils() {}
}
