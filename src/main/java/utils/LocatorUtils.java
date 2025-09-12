package utils;

import exceptions.LocatorException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Utility to fetch By locators (normal & relative).
 */
public final class LocatorUtils {

    private static final Properties LOCATORS = new Properties();
    private static final Logger logger = Logger.getLogger(LocatorUtils.class.getName());

    static {
        try (FileInputStream fis = new FileInputStream("src/test/resources/config/locators.properties")) {
            LOCATORS.load(fis);
            logger.info(() -> "✅ Loaded locators.properties");
        } catch (IOException e) {
            throw new LocatorException("Failed to load locators.properties", e);
        }
    }

    private LocatorUtils() {
    } // prevent instantiation

    public static By get(String key) {
        String raw = LOCATORS.getProperty(key);
        if (raw == null) {
            throw new LocatorException("Locator not found for key: " + key);
        }

        return raw.startsWith("relative:") ? buildRelativeBy(raw) : parseSimple(raw, key);
    }

    public static String getRaw(String key) {
        return LOCATORS.getProperty(key);
    }

    private static By parseSimple(String value, String key) {
        String[] parts = value.split(":", 2);
        if (parts.length != 2) {
            throw new LocatorException("Invalid locator format for key: " + key + " -> " + value);
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
            default -> throw new LocatorException("Unsupported locator type: " + type + " for key: " + key);
        };
    }

    private static By buildRelativeBy(String raw) {
        String[] parts = raw.split("\\|");
        By baseBy = parseBase(parts[0]);
        var relBy = RelativeLocator.with(baseBy);

        for (int i = 1; i < parts.length; i++) {
            String condition = parts[i].trim();

            if (condition.contains("=")) {           // only one "guard" condition
                String[] tokens = condition.split("=", 2);
                String operator = tokens[0].toLowerCase();
                String refKey = tokens[1].trim();

                WebElement refEl = findReferenceElement(refKey);
                if (refEl != null) {                  // execute only if element exists
                    relBy = applyRelativeCondition(relBy, operator, refKey, refEl);
                }
            }
        }
        return relBy;
    }

    // Extracted to reduce complexity
    private static WebElement findReferenceElement(String refKey) {
        if (DriverFactory.getDriver() == null) return null;
        try {
            return DriverFactory.getDriver().findElement(get(refKey));
        } catch (Exception ignored) {
            return null;
        }
    }

    // Extracted to reduce cognitive complexity
    private static RelativeLocator.RelativeBy applyRelativeCondition(RelativeLocator.RelativeBy relBy,
                                                                     String operator,
                                                                     String refKey,
                                                                     WebElement refEl) {
        return switch (operator) {
            case "below" -> relBy.below(refEl);
            case "above" -> relBy.above(refEl);
            case "toleftof" -> relBy.toLeftOf(refEl);
            case "torightof" -> relBy.toRightOf(refEl);
            case "near" -> {
                int distance = 50;
                if (refKey.contains(":")) {
                    String[] nearTokens = refKey.split(":");
                    refKey = nearTokens[0].trim();
                    distance = Integer.parseInt(nearTokens[1].trim());
                    WebElement newRef = findReferenceElement(refKey);
                    if (newRef != null) refEl = newRef;
                }
                yield relBy.near(refEl, distance);
            }
            default -> throw new LocatorException(
                    String.format("Unknown relative condition: %s", operator));
        };
    }


    private static By parseBase(String base) {
        String[] tokens = base.split(":");
        if (tokens.length < 3) {
            throw new LocatorException("Invalid relative base: " + base);
        }
        String type = tokens[1].trim().toLowerCase();
        String value = tokens[2].trim();

        return switch (type) {
            case "id" -> By.id(value);
            case "css" -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            case "tag" -> By.tagName(value);
            default -> throw new LocatorException("Unsupported relative base type: " + type);
        };
    }
}
