package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to load environment-specific configuration properties.
 * Properties are loaded once during class initialization.
 */
public final class Config {

    private static final Properties props = new Properties();
    private static final Logger logger = Logger.getLogger(Config.class.getName());

    static {
        String env = System.getProperty("env", "sit");
        String path = "src/test/resources/config/env/" + env + ".properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            logger.log(Level.INFO, "✅ Loaded environment config: {0}", path);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "❌ Failed to load env config: {0}", path);
            throw new IllegalStateException("Failed to load env config: " + path, e);
        }
    }

    private Config() {
        // prevent instantiation
    }

    /**
     * Retrieves a property value by key.
     *
     * @param key property key
     * @return property value or null if missing
     */
    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            logger.warning(() -> "⚠️ Missing property: " + key);
        }
        return value;
    }

}
