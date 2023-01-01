package app.auditranscribe.utils;

import app.auditranscribe.generic.exceptions.ValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {
    // Arithmetic-related methods
    @Test
    void log2() {
        assertEquals(2, MathUtils.log2(4), 1e-10);
        assertEquals(2.3219280949, MathUtils.log2(5), 1e-10);
        assertEquals(7, MathUtils.log2(128), 1e-10);
    }

    // Miscellaneous mathematical methods
    @Test
    void round() {
        assertEquals(1.23, MathUtils.round(1.23, 2));
        assertEquals(1.23, MathUtils.round(1.23456, 2));
        assertEquals(1, MathUtils.round(1, 3));
        assertEquals(Double.NaN, MathUtils.round(Double.NaN, 4));

        assertThrowsExactly(ValueException.class, () -> MathUtils.round(123.45, -1));
    }
}