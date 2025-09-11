package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import steps.Hooks;

import java.util.logging.Logger;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static void initDriver(String browser) {
        Logger logger = Hooks.getLogger();
        logger.info("Launching browser: " + browser);

        switch (browser.toLowerCase()) {
            case "chrome":
                driver.set(new ChromeDriver());
                break;
            case "firefox":
                driver.set(new FirefoxDriver());
                break;
            case "edge":
                driver.set(new EdgeDriver());
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        driver.get().manage().window().maximize();
        logger.info("Browser launched and maximized successfully.");
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void quitDriver() {
        Logger logger = Hooks.getLogger();
        if (driver.get() != null) {
            driver.get().quit();
            logger.info("Browser closed successfully.");
            driver.remove();
        }
    }
}
