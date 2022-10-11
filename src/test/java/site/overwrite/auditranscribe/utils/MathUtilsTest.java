/*
 * MathUtilsTest.java
 * Description: Test `MathUtils.java`.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.misc.exceptions.ValueException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {
    // Arithmetic-Related methods
    @Test
    void log2() {
        assertEquals(2, MathUtils.log2(4), 0.001);
        assertEquals(2.322, MathUtils.log2(5), 0.001);
        assertEquals(7, MathUtils.log2(128), 0.001);
    }

    @Test
    void logN() {
        assertEquals(2.322, MathUtils.logN(5, 2), 0.001);
        assertEquals(1, MathUtils.logN(Math.PI, Math.PI), 0.001);
        assertEquals(2.090, MathUtils.logN(123, 10), 0.001);
        assertEquals(1.272, MathUtils.logN(456, 123), 0.001);
        assertEquals(-3.183, MathUtils.logN(789, 0.123), 0.001);

        assertThrowsExactly(ValueException.class, () -> MathUtils.logN(123, 1));
        assertThrowsExactly(ValueException.class, () -> MathUtils.logN(123, 0));
        assertThrowsExactly(ValueException.class, () -> MathUtils.logN(123, -1.23));
    }

    @Test
    void modWithMersennePrime() {
        assertEquals(1, MathUtils.modWithMersennePrime(10, 2));
        assertEquals(4, MathUtils.modWithMersennePrime(11, 3));
        assertEquals(67, MathUtils.modWithMersennePrime(1337, 7));
        assertEquals(32, MathUtils.modWithMersennePrime(65536, 11));
        assertEquals(7382, MathUtils.modWithMersennePrime(-9000, 13));
    }

    @Test
    void norm() {
        assertEquals(5., MathUtils.norm(new double[]{3, 4}), 0.01);
        assertEquals(19.105, MathUtils.norm(new double[]{10, 11, 12}), 0.001);
        assertEquals(16.882, MathUtils.norm(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9}), 0.001);
    }

    @Test
    void mean() {
        assertEquals(2.5, MathUtils.mean(new double[]{1, 2, 3, 4}), 0.001);
        assertEquals(3, MathUtils.mean(new double[]{1, 2, 3, 4, 5}), 0.001);
        assertEquals(9, MathUtils.mean(new double[]{10, 8, 13, 9, 11, 14, 6, 4, 12, 7, 5}), 0.001);
        assertEquals(7.50, MathUtils.mean(new double[]{8.04, 6.95, 7.58, 8.81, 8.33, 9.96, 7.24, 4.26, 10.84, 4.82, 5.68}), 0.001);
    }

    @Test
    void argmax() {
        assertEquals(1, MathUtils.argmax(new double[]{1, 3, 2}));
        assertEquals(5, MathUtils.argmax(new double[]{10, 11, 12, 13, 14, 15}));
        assertEquals(1, MathUtils.argmax(new double[]{0, 5, 2, 3, 4, 5}));
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

    @Test
    void round() {
        // Double rounding
        assertEquals(1.23, MathUtils.round(1.23, 2));
        assertEquals(1.23, MathUtils.round(1.23456, 2));
        assertEquals(1, MathUtils.round(1, 3));
        assertEquals(Double.NaN, MathUtils.round(Double.NaN, 4));

        // Float rounding
        assertEquals(1.23f, MathUtils.round(1.23f, 2));
        assertEquals(1.23f, MathUtils.round(1.23456f, 2));
        assertEquals(1f, MathUtils.round(1f, 3));

        // Exception handling
        assertThrowsExactly(ValueException.class, () -> MathUtils.round(123.45, -1));
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
    void isInteger() {
        assertTrue(MathUtils.isInteger(3.));
        assertFalse(MathUtils.isInteger(123.456));
    }

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

    // Misc methods
    @Test
    void numTwoFactors() {
        assertEquals(4, MathUtils.numTwoFactors(16));
        assertEquals(4, MathUtils.numTwoFactors(80));
        assertEquals(0, MathUtils.numTwoFactors(-1));
    }

    @Test
    void wrapValue() {
        assertEquals(9, MathUtils.wrapValue(-1, 0, 10));
        assertEquals(0, MathUtils.wrapValue(0, 0, 10));
        assertEquals(1, MathUtils.wrapValue(1, 0, 10));
        assertEquals(9, MathUtils.wrapValue(9, 0, 10));
        assertEquals(0, MathUtils.wrapValue(10, 0, 10));
        assertEquals(1, MathUtils.wrapValue(11, 0, 10));

        assertEquals(
                new BigDecimal("0").doubleValue(),
                MathUtils.wrapValue(
                        new BigDecimal("10.00000001"),
                        new BigDecimal("0"),
                        new BigDecimal("10")
                ).doubleValue(),
                1e-6
        );
        assertEquals(
                new BigDecimal("10").doubleValue(),
                MathUtils.wrapValue(
                        new BigDecimal("10"),
                        new BigDecimal("0"),
                        new BigDecimal("10")
                ).doubleValue(),
                1e-6
        );
        assertEquals(
                new BigDecimal("9.99999999").doubleValue(),
                MathUtils.wrapValue(
                        new BigDecimal("9.99999999"),
                        new BigDecimal("0"),
                        new BigDecimal("10")
                ).doubleValue(),
                1e-6
        );
        assertEquals(
                new BigDecimal("0.000001").doubleValue(),
                MathUtils.wrapValue(
                        new BigDecimal("0.000001"),
                        new BigDecimal("0"),
                        new BigDecimal("10")
                ).doubleValue(),
                1e-6
        );
        assertEquals(
                new BigDecimal("0").doubleValue(),
                MathUtils.wrapValue(
                        new BigDecimal("0"),
                        new BigDecimal("0"),
                        new BigDecimal("10")
                ).doubleValue(),
                1e-6
        );
        assertEquals(
                new BigDecimal("10").doubleValue(),
                MathUtils.wrapValue(
                        new BigDecimal("-0.000001"),
                        new BigDecimal("0"),
                        new BigDecimal("10")
                ).doubleValue(),
                1e-6
        );

        assertThrowsExactly(ValueException.class, () -> MathUtils.wrapValue(1, -2, -1));
        assertThrowsExactly(ValueException.class, () -> MathUtils.wrapValue(0, 2, 1));
        assertThrowsExactly(ValueException.class, () -> MathUtils.wrapValue(0, 1, 1));

        assertThrowsExactly(ValueException.class, () -> MathUtils.wrapValue(new BigDecimal("1.2"), new BigDecimal("-2"), new BigDecimal("-1")));
        assertThrowsExactly(ValueException.class, () -> MathUtils.wrapValue(new BigDecimal("1.2"), new BigDecimal("2"), new BigDecimal("1")));
        assertThrowsExactly(ValueException.class, () -> MathUtils.wrapValue(BigDecimal.ZERO, new BigDecimal("1"), new BigDecimal("1")));
    }
}