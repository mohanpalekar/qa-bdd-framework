package runners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Main TestNG–Cucumber runner.
 * Cleans screenshots/log folders before running tests.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com/qa/bdd/steps",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/json/cucumber.json",
                "rerun:target/cucumber-reports/rerun/rerun.txt",
                "timeline:target/threads-report/",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        tags = "@api or @ui"
)
public class TestRunner extends AbstractTestNGCucumberTests {

    private static final Logger LOGGER = Logger.getLogger(TestRunner.class.getName());
    private static final Path SCREENSHOT_DIR = Paths.get("target/screenshots");
    private static final Path LOG_DIR = Paths.get("logs");
    private static final Path JSON_REPORT_SOURCE = Paths.get("target/cucumber-reports/json/cucumber.json");
    private static final Path REPORT_DESTINATION_DIR = Paths.get("dashboard/json-files/");
    private static final Path INDEX_FILE_PATH = Paths.get("dashboard/json-files/index.json");
    private static final int MAX_REPORTS = 5;

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
    private void cleanDirectory(Path dir) {
        try {
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
                LOGGER.info(() -> "📂 Created directory: " + dir);
                return;
            }

            try (Stream<Path> files = Files.list(dir)) {
                files
                        .filter(Files::isRegularFile)
                        .forEach(this::deleteFileQuietly);
            }

            LOGGER.info(() -> "🧹 Cleaned directory: " + dir);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "❌ Could not clean " + dir);
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

    /**
     * Copies the new JSON report and rebuilds the index.json from all existing reports.
     */
    @AfterSuite(alwaysRun = true)
    public void processReports() {
        LOGGER.info(() -> "📋 Starting report processing…");

        if (!Files.exists(JSON_REPORT_SOURCE)) {
            LOGGER.warning(() -> "⚠️ JSON report not found at: " + JSON_REPORT_SOURCE);
            return;
        }

        try {
            Files.createDirectories(REPORT_DESTINATION_DIR);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM_dd_yyyy_h_mm_ss_a"));
            String newFileName = "report_" + timestamp.replace(" ", "_") + ".json";
            Path newReportPath = REPORT_DESTINATION_DIR.resolve(newFileName);

            Files.copy(JSON_REPORT_SOURCE, newReportPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info(() -> "✅ JSON report copied and renamed to: " + newReportPath);

            rebuildIndexFile();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "❌ Failed to copy and rename JSON report.");
        }
    }

    /**
     * Scans the report directory, sorts files by modification time, and writes
     * the latest 5 to index.json.
     */
    private void rebuildIndexFile() {
        if (!Files.exists(REPORT_DESTINATION_DIR)) {
            LOGGER.warning(() -> "⚠️ Report directory not found: " + REPORT_DESTINATION_DIR);
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray reportList = new JsonArray();

        try (Stream<Path> files = Files.list(REPORT_DESTINATION_DIR)) {
            var latestReports = files
                    .filter(file -> file.toString().endsWith(".json") && !file.getFileName().toString().equals(INDEX_FILE_PATH.getFileName().toString()))
                    .sorted(Comparator.comparingLong(this::getLastModifiedTime).reversed())
                    .limit(MAX_REPORTS)
                    .toList();

            for (Path file : latestReports) {
                JsonObject reportObject = new JsonObject();
                String fileName = file.getFileName().toString();
                reportObject.addProperty("name", fileName);
                reportObject.addProperty("url", REPORT_DESTINATION_DIR.getFileName().toString() + "/" + fileName);
                reportList.add(reportObject);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "❌ Failed to scan report directory.");
            return;
        }

        try (FileWriter writer = new FileWriter(INDEX_FILE_PATH.toFile())) {
            gson.toJson(reportList, writer);
            LOGGER.info(() -> "✅ Rebuilt index.json with the latest " + reportList.size() + " reports.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "❌ Failed to write to index.json.");
        }
    }

    /**
     * Helper method to get the last modified time of a file.
     */
    private long getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e, () -> "❌ Could not get last modified time for " + path);
            return 0;
        }
    }
}