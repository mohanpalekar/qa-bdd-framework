package context;

import io.restassured.response.Response;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe scenario context for UI & API tests.
 * UtilityClass from Lombok makes all methods static automatically.
 */
@UtilityClass
public class ScenarioContext {

    // One map per thread/scenario
    private static final ThreadLocal<Map<String, Object>> context =
            ThreadLocal.withInitial(HashMap::new);

    // Generic put/get
    public void put(String key, Object value) {
        context.get().put(key, value);
    }

    public Object get(String key) {
        return context.get().get(key);
    }

    public String getString(String key) {
        Object val = get(key);
        return val == null ? null : val.toString();
    }

    /**
     * Clear context map and remove the ThreadLocal
     * to avoid memory leaks when threads are reused.
     */
    public void clear() {
        context.get().clear();
        context.remove();  // ✅ release the ThreadLocal reference
    }

    // Keep compatibility for Response
    public void saveResponse(Response response) {
        put("_response", response);
    }

    public Response getResponse() {
        return (Response) get("_response");
    }
}
