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

    @Test
    void ceilDiv() {
        assertEquals(3, MathUtils.ceilDiv(8, 3));
        assertEquals(3, MathUtils.ceilDiv(9, 3));
        assertEquals(4, MathUtils.ceilDiv(10, 3));
        assertEquals(4, MathUtils.ceilDiv(11, 3));
        assertEquals(4, MathUtils.ceilDiv(12, 3));

        assertEquals(3, MathUtils.ceilDiv(654, 321));
        assertEquals(4, MathUtils.ceilDiv(987, 321));
        assertEquals(3, MathUtils.ceilDiv(789, 321));
    }

    // Data-related methods
    @Test
    void intLerp() {
        assertEquals(5, MathUtils.intLerp(0, 10, 0.567), 0.001);
        assertEquals(1, MathUtils.intLerp(0, 10, 0.123), 0.001);
        assertEquals(9, MathUtils.intLerp(0, 10, 0.901), 0.001);
        assertEquals(2, MathUtils.intLerp(0, 10, 0.234), 0.001);
    }

    @Test
    void lerp() {
        assertEquals(5.67, MathUtils.lerp(0., 10., 0.567), 0.001);
        assertEquals(1.23, MathUtils.lerp(0., 10., 0.123), 0.001);
        assertEquals(9.01, MathUtils.lerp(0., 10., 0.901), 0.001);
        assertEquals(2.34, MathUtils.lerp(0., 10., 0.234), 0.001);
    }

    @Test
    void normalize() {
        // Test extreme values
        assertEquals(0, MathUtils.normalize(1, 1, 5));
        assertEquals(1, MathUtils.normalize(5, 1, 5));

        assertEquals(0, MathUtils.normalize(3, 3, 9));
        assertEquals(1, MathUtils.normalize(9, 3, 9));

        // Test in between
        assertEquals(0.375, MathUtils.normalize(2.5, 1, 5), 0.0001);
        assertEquals(0.535, MathUtils.normalize(3.14, 1, 5), 0.0001);

        assertEquals(0.055, MathUtils.normalize(3.33, 3, 9), 0.0001);
        assertEquals(0.333, MathUtils.normalize(5, 3, 9), 0.001);
        assertEquals(0.667, MathUtils.normalize(7, 3, 9), 0.001);

        // Test different endpoints
        assertEquals(0.2345, MathUtils.normalize(2.345, -10, 10, -1, 1), 1e-4);
        assertEquals(4.3571, MathUtils.normalize(4, -1, 13, 4, 5), 1e-4);
    }

    // Combinatorial methods
    @Test
    void selfProduct() {
        // Test base case
        int[][] baseCaseTest1 = MathUtils.selfProduct(2, 0);
        int[][] baseCaseTest2 = MathUtils.selfProduct(3, 0);
        int[][] baseCaseTest3 = MathUtils.selfProduct(5, 0);

        for (int[][] baseCaseTest : new int[][][]{baseCaseTest1, baseCaseTest2, baseCaseTest3}) {
            assertEquals(0, baseCaseTest.length);
        }

        // Test other values
        int[][] selfProductTest1 = MathUtils.selfProduct(2, 5);
        int[][] selfProductTest2 = MathUtils.selfProduct(5, 3);
        int[][] selfProductTest3 = MathUtils.selfProduct(8, 1);

        int[] expectedLengths = {32, 125, 8};

        int i = 0;
        for (int[][] selfProductTest : new int[][][]{selfProductTest1, selfProductTest2, selfProductTest3}) {
            assertEquals(expectedLengths[i], selfProductTest.length);
            i++;
        }

        assertArrayEquals(new int[]{0, 1, 1, 0, 1}, selfProductTest1[13]);
        assertArrayEquals(new int[]{2, 3, 2}, selfProductTest2[67]);
        assertArrayEquals(new int[]{5}, selfProductTest3[5]);
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
    void numTwoFactors() {
        assertEquals(6, MathUtils.numTwoFactors(1344));
        assertEquals(4, MathUtils.numTwoFactors(80));
        assertEquals(4, MathUtils.numTwoFactors(16));
        assertEquals(0, MathUtils.numTwoFactors(1));
        assertEquals(0, MathUtils.numTwoFactors(0));
        assertEquals(0, MathUtils.numTwoFactors(-1));
        assertEquals(4, MathUtils.numTwoFactors(-16));
        assertEquals(4, MathUtils.numTwoFactors(-80));
        assertEquals(6, MathUtils.numTwoFactors(-1344));
    }

    @Test
    void round() {
        assertEquals(1.23, MathUtils.round(1.23, 2));
        assertEquals(1.23, MathUtils.round(1.23456, 2));
        assertEquals(1, MathUtils.round(1, 3));
        assertEquals(Double.NaN, MathUtils.round(Double.NaN, 4));

        assertThrowsExactly(ValueException.class, () -> MathUtils.round(123.45, -1));
    }
}