package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.qa.bdd.steps.Hooks;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Thread-safe WebDriver factory using ThreadLocal.
 * Supports Chrome, Firefox, and Edge.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final Logger logger = Hooks.getLogger();

    private DriverFactory() {
        // prevent instantiation
    }

    /**
     * Initializes a WebDriver for the given browser type and maximizes the window.
     *
     * @param browser browser type (chrome, firefox, edge)
     */
    public static void initDriver(String browser) {
        String normalized = browser.toLowerCase(Locale.ROOT);
        logger.info(() -> "Launching browser: " + normalized);

        switch (normalized) {
            case "chrome" -> DRIVER.set(new ChromeDriver());
            case "firefox" -> DRIVER.set(new FirefoxDriver());
            case "edge" -> DRIVER.set(new EdgeDriver());
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        DRIVER.get().manage().window().maximize();
        logger.info(() -> "Browser launched and maximized successfully.");
    }

    /**
     * Returns the WebDriver instance for the current thread.
     *
     * @return thread-local WebDriver
     */
    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    /**
     * Quits the WebDriver for the current thread and removes it from ThreadLocal.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            logger.info(() -> "Browser closed successfully.");
            DRIVER.remove();
        }
    }
}
