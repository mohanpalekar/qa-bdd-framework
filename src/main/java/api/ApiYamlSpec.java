package api;

import lombok.Data;

import java.util.Map;

@Data
public class ApiYamlSpec {
    private String name;
    private String method;
    private String uri;
    private Map<String, String> headers;
    private Map<String, String> query;
    private Map<String, String> pathParams;
    private String payload;
}
