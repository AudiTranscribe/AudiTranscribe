package app.auditranscribe.utils;

import app.auditranscribe.generic.exceptions.ValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {
    // Arithmetic-related methods
    @Test
    void binlog() {
        assertEquals(2, MathUtils.binlog(4));
        assertEquals(2, MathUtils.binlog(5));
        assertEquals(7, MathUtils.binlog(128));
    }

    @Test
    void log2() {
        assertEquals(2, MathUtils.log2(4), 1e-10);
        assertEquals(2.3219280949, MathUtils.log2(5), 1e-10);
        assertEquals(7, MathUtils.log2(128), 1e-10);
    }

    // Checking-related methods
    @Test
    void isPowerOf2() {
        assertTrue(MathUtils.isPowerOf2(1));
        assertTrue(MathUtils.isPowerOf2(4));
        assertTrue(MathUtils.isPowerOf2(2048));
        assertTrue(MathUtils.isPowerOf2(65536));

        assertFalse(MathUtils.isPowerOf2(3));
        assertFalse(MathUtils.isPowerOf2(5));
        assertFalse(MathUtils.isPowerOf2(10012));
        assertFalse(MathUtils.isPowerOf2(123456));

        assertThrowsExactly(ValueException.class, () -> MathUtils.isPowerOf2(0));
        assertThrowsExactly(ValueException.class, () -> MathUtils.isPowerOf2(-4));
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