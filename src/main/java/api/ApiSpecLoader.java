package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility to load API YAML specifications from resources/api-specs.
 */
public final class ApiSpecLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final String SPEC_BASE_PATH = "api-specs/";

    private ApiSpecLoader() {
        // utility class
    }

    /**
     * Loads and parses a YAML spec file into {@link ApiYamlSpec}.
     *
     * @param yamlFileName file name of the YAML spec (relative to api-specs folder)
     * @return parsed {@link ApiYamlSpec}
     * @throws IllegalArgumentException if yamlFileName is null/blank or file not found
     * @throws IllegalStateException    if reading or parsing fails
     */
    public static ApiYamlSpec load(final String yamlFileName) {
        if (yamlFileName == null || yamlFileName.isBlank()) {
            throw new IllegalArgumentException("YAML file name cannot be null or blank");
        }

        final String resourcePath = SPEC_BASE_PATH + yamlFileName;

        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new IllegalArgumentException("YAML spec file not found on classpath: " + resourcePath);
            }

            return YAML_MAPPER.readValue(is, ApiYamlSpec.class);

        } catch (IOException e) {
            // wrap as unchecked — test will simply fail
            throw new IllegalStateException("Failed to load YAML spec: " + resourcePath, e);
        }
    }
}
