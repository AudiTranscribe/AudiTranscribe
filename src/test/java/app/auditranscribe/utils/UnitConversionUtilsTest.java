package app.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionUtilsTest {
    // Audio unit conversion
    @Test
    void hzToOctaves() {
        assertEquals(4, UnitConversionUtils.hzToOctaves(440), 1e-10);
        assertEquals(6.2186402865, UnitConversionUtils.hzToOctaves(2048), 1e-10);
        assertEquals(8.8102795025, UnitConversionUtils.hzToOctaves(12345), 1e-10);
    }
}