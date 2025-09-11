package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = "steps",
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

    private static final Logger logger = Logger.getLogger(TestRunner.class.getName());
    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final String LOG_DIR = "logs";

    @Override
    @DataProvider(parallel = true)   // enable parallel scenario execution
    public Object[][] scenarios() {
        return super.scenarios();
    }


    @BeforeSuite(alwaysRun = true)
    public void cleanUpArtifacts() {
        logger.info("🚀 Starting test suite cleanup...");
        cleanDirectory(SCREENSHOT_DIR);
        cleanDirectory(LOG_DIR);
        logger.info("✅ Cleanup complete. Starting tests...");
    }

    private void cleanDirectory(String dirPath) {
        Path dir = Paths.get(dirPath);
        try {
            if (Files.exists(dir)) {
                Files.list(dir)
                        .filter(Files::isRegularFile)
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                                logger.fine("🧹 Deleted: " + p);
                            } catch (IOException ex) {
                                logger.log(Level.WARNING, "⚠️ Could not delete " + p, ex);
                            }
                        });
            } else {
                Files.createDirectories(dir);
                logger.info("📂 Created directory: " + dirPath);
            }
            logger.info("🧹 Cleaned directory: " + dirPath);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "❌ Could not clean " + dirPath, e);
        }
    }
}
