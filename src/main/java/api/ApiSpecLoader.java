package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class ApiSpecLoader {
    public static ApiYamlSpec load(String yamlFileName) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(new File("src/test/resources/api-specs/" + yamlFileName), ApiYamlSpec.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML spec: " + yamlFileName, e);
        }
    }
}
