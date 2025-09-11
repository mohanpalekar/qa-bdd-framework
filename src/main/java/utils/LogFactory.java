package utils;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

public final class LogFactory {
    private static final ConcurrentHashMap<String, Logger> SCENARIO_LOGGERS = new ConcurrentHashMap<>();

    private LogFactory() {
        // utility
    }

    public static Logger createScenarioLogger(String scenarioName) {
        return SCENARIO_LOGGERS.computeIfAbsent(scenarioName, key -> {
            Logger logger = Logger.getLogger("SCENARIO-" + key);
            logger.setUseParentHandlers(false);

            try {
                Level level = Level.parse(Config.get("log.level")); // fallback
                logger.setLevel(level);
            } catch (Exception e) {
                logger.setLevel(Level.INFO);
            }

            String ts = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String safe = key.replaceAll("[^a-zA-Z0-9-_]", "_");

            Path dir = Paths.get("logs");
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create logs directory", e);
            }

            Path file = dir.resolve(safe + "-" + ts + ".log");
            try {
                FileHandler fh = new FileHandler(file.toString(), true); // append safe
                fh.setFormatter(new SimpleFormatter() {
                    private static final String FORMAT = "%1$tF %1$tT [%4$s] %5$s %n";

                    @Override
                    public synchronized String format(LogRecord lr) {
                        return String.format(FORMAT,
                                new Date(lr.getMillis()),
                                lr.getSourceClassName(),
                                lr.getLoggerName(),
                                lr.getLevel().getLocalizedName(),
                                lr.getMessage());
                    }
                });
                logger.addHandler(fh);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create log file for scenario " + key, e);
            }

            return logger;
        });
    }
}
