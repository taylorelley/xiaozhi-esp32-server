package xiaozhi.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ResultTest {

    @Test
    void freshResultDefaultsToSuccess() {
        Result<String> r = new Result<>();
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMsg());
        assertNull(r.getData());
    }

    @Test
    void okAttachesDataAndReturnsSameInstance() {
        Result<String> r = new Result<>();
        Result<String> returned = r.ok("payload");

        // Fluent API: the call must return 'this'.
        assertSame(r, returned);
        assertEquals("payload", r.getData());
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMsg());
    }

    @Test
    void errorWithCodeAndMessageSetsBothAndReturnsSameInstance() {
        Result<Object> r = new Result<>();
        Result<Object> returned = r.error(10042, "device not bound");

        assertSame(r, returned);
        assertEquals(10042, r.getCode());
        assertEquals("device not bound", r.getMsg());
    }

    @Test
    void errorDoesNotTouchDataField() {
        Result<String> r = new Result<>();
        r.ok("original");
        r.error(123, "boom");

        // ok() set the data; error(code, msg) must not wipe it.
        assertEquals("original", r.getData());
        assertEquals(123, r.getCode());
        assertEquals("boom", r.getMsg());
    }

    @Test
    void setters_fromLombokDataAnnotation_work() {
        Result<Integer> r = new Result<>();
        r.setCode(401);
        r.setMsg("unauthorized");
        r.setData(42);

        assertEquals(401, r.getCode());
        assertEquals("unauthorized", r.getMsg());
        assertEquals(Integer.valueOf(42), r.getData());
    }

    @Test
    void genericDataTypeIsPreserved() {
        Result<java.util.List<String>> r = new Result<>();
        r.ok(java.util.Arrays.asList("a", "b"));
        assertNotNull(r.getData());
        assertEquals(2, r.getData().size());
    }
}
