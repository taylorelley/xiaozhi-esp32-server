package xiaozhi.common.exception;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sanity checks for the ErrorCode interface. Two goals:
 *   1) Catch accidental duplicates when someone adds a new code.
 *   2) Document the small handful of codes referenced elsewhere in tests
 *      (and production code), so renaming/removing one fails fast.
 */
class ErrorCodeTest {

    @Test
    void allErrorCodesAreUnique() throws IllegalAccessException {
        Map<Integer, String> seen = new HashMap<>();
        for (Field f : ErrorCode.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())
                    && Modifier.isFinal(f.getModifiers())
                    && f.getType() == int.class) {
                int value = f.getInt(null);
                String prior = seen.putIfAbsent(value, f.getName());
                assertEquals(null, prior,
                        "duplicate error code " + value + ": " + prior + " and " + f.getName());
            }
        }
        assertTrue(seen.size() >= 50, "expected a substantial ErrorCode surface, got " + seen.size());
    }

    @Test
    void wellKnownHttpStatusCodes() {
        assertEquals(500, ErrorCode.INTERNAL_SERVER_ERROR);
        assertEquals(401, ErrorCode.UNAUTHORIZED);
        assertEquals(403, ErrorCode.FORBIDDEN);
    }

    @Test
    void otaCodesUsedByPythonClientAreStable() {
        // These two are part of the contract with main/xiaozhi-server/config/manage_api_client.py.
        assertEquals(10041, ErrorCode.OTA_DEVICE_NOT_FOUND);
        assertEquals(10042, ErrorCode.OTA_DEVICE_NEED_BIND);
    }
}
