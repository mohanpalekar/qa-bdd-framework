package utils;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

public final class LogFactory {

    private static final ConcurrentHashMap<String, Logger> SCENARIO_LOGGERS = new ConcurrentHashMap<>();
    private static final String LOG_DIR = "logs";
    private static final String LOG_DATE_FORMAT = "yyyyMMdd-HHmmss";
    private static final String LOG_FORMAT = "%1$tF %1$tT [%4$s] %5$s %n";

    private LogFactory() {
        // utility class
    }

    public static Logger createScenarioLogger(String scenarioName) {
        return SCENARIO_LOGGERS.computeIfAbsent(scenarioName, key -> {
            Logger logger = Logger.getLogger("SCENARIO-" + key);
            logger.setUseParentHandlers(false);

            try {
                Level level = Level.parse(Config.get("log.level"));
                logger.setLevel(level);
            } catch (Exception e) {
                logger.setLevel(Level.INFO);
            }

            Path logFile = prepareLogFile(key);
            try {
                FileHandler fh = createFileHandler(logFile.toString());
                logger.addHandler(fh);
            } catch (IOException e) {
                throw new LoggerInitializationException("Failed to create log file for scenario: " + key, e);
            }

            return logger;
        });
    }

    private static Path prepareLogFile(String scenarioName) {
        String ts = new SimpleDateFormat(LOG_DATE_FORMAT).format(new Date());
        String safeName = scenarioName.replaceAll("[^a-zA-Z0-9-_]", "_");
        Path dir = Paths.get(LOG_DIR);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new LoggerInitializationException("Failed to create logs directory: " + LOG_DIR, e);
        }
        return dir.resolve(safeName + "-" + ts + ".log");
    }

    private static FileHandler createFileHandler(String filePath) throws IOException {
        FileHandler fh = new FileHandler(filePath, true);
        fh.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord lr) {
                return String.format(LOG_FORMAT,
                        new Date(lr.getMillis()),
                        lr.getSourceClassName(),
                        lr.getLoggerName(),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage());
            }
        });
        return fh;
    }

    // Custom exception
    public static class LoggerInitializationException extends RuntimeException {
        public LoggerInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
