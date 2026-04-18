package xiaozhi.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * We only exercise the string-message constructors here. The int-code variants
 * resolve the message from the Spring MessageSource via MessageUtils, and
 * standing up a Spring context for a unit test would be disproportionate.
 */
class RenExceptionTest {

    @Test
    void isRuntimeException() {
        assertTrue(RuntimeException.class.isAssignableFrom(RenException.class));
    }

    @Test
    void stringConstructorUsesInternalServerErrorCode() {
        RenException ex = new RenException("boom");
        assertEquals("boom", ex.getMessage());
        assertEquals("boom", ex.getMsg());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, ex.getCode());
        assertNull(ex.getCause());
    }

    @Test
    void stringAndCauseConstructorWiresBothThrough() {
        IllegalStateException cause = new IllegalStateException("underlying");
        RenException ex = new RenException("wrapper", cause);

        assertEquals("wrapper", ex.getMessage());
        assertEquals("wrapper", ex.getMsg());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, ex.getCode());
        assertSame(cause, ex.getCause());
    }

    @Test
    void settersUpdateCodeAndMessage() {
        RenException ex = new RenException("initial");
        ex.setCode(10042);
        ex.setMsg("device not bound");

        assertEquals(10042, ex.getCode());
        assertEquals("device not bound", ex.getMsg());
        // The underlying Throwable message is not rewritten by setMsg; that's expected.
        assertNotNull(ex.getMessage());
    }

    @Test
    void canBeThrownAndCaught() {
        try {
            throw new RenException("thrown");
        } catch (RenException caught) {
            assertEquals("thrown", caught.getMsg());
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, caught.getCode());
        }
    }
}
