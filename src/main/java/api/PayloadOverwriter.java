package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.io.InputStream;
import java.util.Map;

public final class PayloadOverwriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // JsonPath config to work with Jackson JsonNode and create missing nodes on set()
    private static final Configuration CONF = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .options(
                    Option.DEFAULT_PATH_LEAF_TO_NULL,
                    Option.SUPPRESS_EXCEPTIONS
            )
            .build();

    private PayloadOverwriter() {
    }

    /**
     * Build request payload by loading default payload JSON from classpath and applying
     * JSONPath overrides (only keys starting with "$.").
     *
     * @param defaultFile classpath-relative under resources/payloads/
     * @param overrides   map of JSONPath -> value (string; will be type-coerced)
     * @return final JSON string
     */
    public static String buildPayload(String defaultFile, Map<String, String> overrides) {
        String path = "payloads/" + defaultFile;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Payload file not found on classpath: " + path);
            }

            JsonNode base = MAPPER.readTree(is);
            DocumentContext ctx = JsonPath.using(CONF).parse(base);

            if (overrides != null) {
                for (Map.Entry<String, String> e : overrides.entrySet()) {
                    String key = e.getKey();
                    if (!key.startsWith("$.")) {
                        // ignore non-JSONPath keys (e.g., path.xxx / query.xxx) if caller passed the full table
                        continue;
                    }
                    Object value = coerceType(e.getValue());
                    ctx.set(key, value);
                }
            }

            JsonNode mutated = ctx.json();
            return MAPPER.writeValueAsString(mutated);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to overwrite payload from: " + path, ex);
        }
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
        if (raw == null) return null;
        String t = raw.trim();

        // boolean
        if ("true".equalsIgnoreCase(t)) return true;
        if ("false".equalsIgnoreCase(t)) return false;

        // null
        if ("null".equalsIgnoreCase(t)) return null;

        // number (int/long/double)
        try {
            if (t.matches("[-+]?\\d+")) {
                long l = Long.parseLong(t);
                // fit into Integer if possible
                return (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) ? (int) l : l;
            }
            if (t.matches("[-+]?\\d*\\.\\d+([eE][-+]?\\d+)?")) {
                return Double.parseDouble(t);
            }
        } catch (NumberFormatException ignored) {
            // fall through to JSON / String
        }

        // JSON object/array literal
        if ((t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"))) {
            try {
                return MAPPER.readTree(t);
            } catch (Exception ignored) {
                // if not valid JSON, fall back to string
            }
        }

        // default: string
        return raw;
    }
}
