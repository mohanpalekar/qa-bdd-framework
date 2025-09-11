package api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utils.Config;

import java.util.Map;

public class RestClient {

    public RestClient() {
        RestAssured.baseURI = Config.get("baseURI");
    }

    public Response call(ApiYamlSpec spec, Map<String, String> pathParams, Map<String, String> queryParams, Map<String, String> payloadOverrides) {
        RequestSpecification req = RestAssured.given();
        if (spec.getHeaders() != null) req.headers(spec.getHeaders());
        if (queryParams != null) req.queryParams(queryParams);
        if (spec.getPayload() != null) {
            String body = PayloadOverwriter.buildPayload(spec.getPayload(), payloadOverrides);
            req.body(body);
        }
        String resolvedUri = resolveUri(spec.getUri(), pathParams);

        return switch (spec.getMethod().toUpperCase()) {
            case "GET" -> req.get(resolvedUri);
            case "POST" -> req.post(resolvedUri);
            case "PUT" -> req.put(resolvedUri);
            case "PATCH" -> req.patch(resolvedUri);
            case "DELETE" -> req.delete(resolvedUri);
            default -> throw new IllegalArgumentException("Unsupported method: " + spec.getMethod());
        };
    }

    private String resolveUri(String uri, Map<String, String> pathParams) {
        String out = uri;
        if (pathParams != null) {
            for (var e : pathParams.entrySet()) out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return out;
    }
}
