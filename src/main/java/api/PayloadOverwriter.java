package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import exceptions.PayloadBuildException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * Utility to build request payloads by loading default payload JSON from classpath
 * and applying JSONPath overrides.
 */
public final class PayloadOverwriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Configuration CONF = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .options(
                    Option.DEFAULT_PATH_LEAF_TO_NULL,
                    Option.SUPPRESS_EXCEPTIONS
            )
            .build();

    private PayloadOverwriter() {
        // utility class
    }

    /**
     * Build request payload by loading default payload JSON from classpath and applying
     * JSONPath overrides.
     *
     * @param defaultFile classpath-relative under resources/payloads/
     * @param overrides   map of JSONPath -> value
     * @return final JSON string
     * @throws PayloadBuildException if payload cannot be built
     */
    public static String buildPayload(final String defaultFile, final Map<String, String> overrides) {
        final String path = "payloads/" + Objects.requireNonNull(defaultFile, "defaultFile must not be null");

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new PayloadBuildException("Payload file not found on classpath: " + path);
            }

            JsonNode base = MAPPER.readTree(is);
            DocumentContext ctx = JsonPath.using(CONF).parse(base);

            applyOverrides(ctx, overrides);

            JsonNode mutated = ctx.json();
            return MAPPER.writeValueAsString(mutated);

        } catch (IOException ex) {
            // only rethrow; don't log here
            throw new PayloadBuildException("Failed to overwrite payload from: " + path, ex);
        }
    }

    /**
     * Apply overrides to the JSON document context.
     */
    private static void applyOverrides(DocumentContext ctx, Map<String, String> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return;
        }
        overrides.forEach((key, value) -> {
            if (key != null && key.startsWith("$.")) {
                ctx.set(key, coerceType(value));
            }
        });
    }

    /**
     * Best-effort type coercion for DataTable string values:
     * - "true"/"false" -> boolean
     * - "null" -> null
     * - integer / decimal numbers -> numeric
     * - JSON object/array strings -> JsonNode
     * - otherwise -> String
     */
    private static Object coerceType(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();

        // booleans
        if ("true".equalsIgnoreCase(t)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(t)) return Boolean.FALSE;

        // null literal
        if ("null".equalsIgnoreCase(t)) return null;

        // number
        if (t.matches("[-+]?\\d+")) {
            long l = Long.parseLong(t);
            return (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) ? (int) l : l;
        }
        if (t.matches("[-+]?\\d*\\.\\d+([eE][-+]?\\d+)?")) {
            return Double.parseDouble(t);
        }

        // JSON literal
        if ((t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"))) {
            try {
                return MAPPER.readTree(t);
            } catch (IOException ignored) {
                // fallback to string
            }
        }

        return raw;
    }
}
