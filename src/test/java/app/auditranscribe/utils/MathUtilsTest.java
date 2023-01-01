package app.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {
    // Arithmetic-Related methods
    @Test
    void log2() {
        assertEquals(2, MathUtils.log2(4), 1e-10);
        assertEquals(2.3219280949, MathUtils.log2(5), 1e-10);
        assertEquals(7, MathUtils.log2(128), 1e-10);
    }
}