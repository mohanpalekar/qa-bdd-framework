package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main TestNG–Cucumber runner.
 * Cleans screenshots/log folders before running tests.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com/qa/bdd/steps",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/html",
                "json:target/cucumber-reports/json",
                "rerun:target/cucumber-reports/rerun/rerun.txt",
                "timeline:target/threads-report/",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        tags = "@ui1"
)
public class TestRunner extends AbstractTestNGCucumberTests {

    private static final Logger LOGGER = Logger.getLogger(TestRunner.class.getName());
    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final String LOG_DIR = "logs";

    /**
     * Override Cucumber scenarios provider to allow parallel execution.
     * Thread count is controlled via -Ddataproviderthreadcount=… at runtime.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    /**
     * Clean artefact folders before any suite runs.
     */
    @BeforeSuite(alwaysRun = true)
    public void cleanUpArtifacts() {
        LOGGER.info(() -> "🚀 Starting test suite cleanup…");
        cleanDirectory(SCREENSHOT_DIR);
        cleanDirectory(LOG_DIR);
        LOGGER.info(() -> "✅ Cleanup complete. Starting tests…");
    }

    /**
     * Deletes all regular files in a directory.
     * Creates the directory if it does not exist.
     */
    private void cleanDirectory(String dirPath) {
        final Path dir = Paths.get(dirPath);

        try {
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
                LOGGER.info(() -> "📂 Created directory: " + dirPath);
                return;
            }

            // try-with-resources when using Streams:
            try (var files = Files.list(dir)) {
                files
                        .filter(Files::isRegularFile)
                        .forEach(this::deleteFileQuietly);
            }

            LOGGER.info(() -> "🧹 Cleaned directory: " + dirPath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "❌ Could not clean " + dirPath);
        }
    }

    /**
     * Delete a file and log on failure.
     */
    private void deleteFileQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
            LOGGER.fine(() -> "🧹 Deleted: " + file);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex, () -> "⚠️ Could not delete " + file);
        }
    }
}
