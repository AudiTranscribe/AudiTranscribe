/*
 * MathUtilsTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-05-07
 *
 * Description: Test `MathUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {
    // Arithmetic-Related methods
    @Test
    void log2() {
        assertEquals(2, MathUtils.log2(4), 0.001);
        assertEquals(2.322, MathUtils.log2(5), 0.001);
    }

    @Test
    void logN() {
        assertEquals(1, MathUtils.logN(Math.PI, Math.PI), 0.001);
        assertEquals(2.090, MathUtils.logN(123, 10), 0.001);
        assertEquals(1.272, MathUtils.logN(456, 123), 0.001);
    }

    @Test
    void modWithMersennePrime() {
        assertEquals(4, MathUtils.modWithMersennePrime(11, 3));
        assertEquals(67, MathUtils.modWithMersennePrime(1337, 7));
        assertEquals(7382, MathUtils.modWithMersennePrime(-9000, 13));
    }

    @Test
    void norm() {
        assertEquals(5., MathUtils.norm(new double[]{3, 4}), 0.01);
        assertEquals(19.105, MathUtils.norm(new double[]{10, 11, 12}), 0.001);
        assertEquals(16.882, MathUtils.norm(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9}), 0.001);
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
    void normalise() {
        // Test extreme values
        assertEquals(0, MathUtils.normalise(1, 1, 5));
        assertEquals(1, MathUtils.normalise(5, 1, 5));

        assertEquals(0, MathUtils.normalise(3, 3, 9));
        assertEquals(1, MathUtils.normalise(9, 3, 9));

        // Test in between
        assertEquals(0.375, MathUtils.normalise(2.5, 1, 5), 0.0001);
        assertEquals(0.535, MathUtils.normalise(3.14, 1, 5), 0.0001);

        assertEquals(0.055, MathUtils.normalise(3.33, 3, 9), 0.0001);
        assertEquals(0.333, MathUtils.normalise(5, 3, 9), 0.001);
        assertEquals(0.667, MathUtils.normalise(7, 3, 9), 0.001);
    }

    @Test
    void roundDouble() {
        assertEquals(1.23, MathUtils.round(1.23, 2));
        assertEquals(1.23, MathUtils.round(1.23456, 2));
        assertEquals(1, MathUtils.round(1, 3));
    }

    @Test
    void roundFloat() {
        assertEquals(1.23f, MathUtils.round(1.23f, 2));
        assertEquals(1.23f, MathUtils.round(1.23456f, 2));
        assertEquals(1f, MathUtils.round(1f, 3));
    }

    // Checking-related methods
    @Test
    void isInteger() {
        assertTrue(MathUtils.isInteger(3.));
        assertFalse(MathUtils.isInteger(123.456));
    }

    // Misc methods
    @Test
    void numTwoFactors() {
        assertEquals(4, MathUtils.numTwoFactors(16));
        assertEquals(4, MathUtils.numTwoFactors(80));
        assertEquals(0, MathUtils.numTwoFactors(-1));
    }
}