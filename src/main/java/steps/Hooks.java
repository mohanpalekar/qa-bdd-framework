package steps;

import context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import utils.DriverFactory;
import utils.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class Hooks {
    private static final ThreadLocal<Logger> scenarioLogger = new ThreadLocal<>();

    @Before
    public void beforeScenario(Scenario scenario) {
        Logger logger = LogFactory.createScenarioLogger(scenario.getName());
        scenarioLogger.set(logger);
        logger.info("🚀 Starting scenario: " + scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        ScenarioContext.clear();
        Logger logger = scenarioLogger.get();
        try {
            if (scenario.isFailed() && DriverFactory.getDriver() != null) {
                TakesScreenshot ts = (TakesScreenshot) DriverFactory.getDriver();
                File src = ts.getScreenshotAs(OutputType.FILE);
                String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
                File dest = new File("target/screenshots/" + safeName + ".png");

                Files.createDirectories(dest.getParentFile().toPath());
                Files.copy(src.toPath(), dest.toPath());

                logger.severe("❌ Scenario failed! Screenshot saved at: " + dest.getAbsolutePath());
                scenario.attach(Files.readAllBytes(dest.toPath()), "image/png", safeName);
            }
        } catch (IOException e) {
            logger.severe("Failed to capture screenshot: " + e.getMessage());
        } finally {
            if (DriverFactory.getDriver() != null) {
                DriverFactory.quitDriver();
                logger.info("🌙 Browser closed after scenario: " + scenario.getName());
            }
            // mark end of scenario
            logger.info("✅ Finished scenario: " + scenario.getName());
            scenarioLogger.remove();
        }
    }


    public static Logger getLogger() {
        return scenarioLogger.get();
    }
}
