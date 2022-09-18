/*
 * StatisticalUtilsTest.java
 * Description: Test `StatisticalUtils.java`.
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
import site.overwrite.auditranscribe.exceptions.generic.LengthException;

import static org.junit.jupiter.api.Assertions.*;

class StatisticalUtilsTest {
    @Test
    void sum() {
        double[] array1 = {1, 2, 3, 4, 5};
        double[] array2 = {1, -2, 3, -4, 5, -6, 7, -8};

        assertEquals(0, StatisticalUtils.sum(new double[0]));
        assertEquals(15, StatisticalUtils.sum(array1));
        assertEquals(-4, StatisticalUtils.sum(array2));
    }

    @Test
    void average() {
        double[] array1 = {1, 2, 3, 4, 5};
        double[] array2 = {1, -2, 3, -4, 5, -6, 7, -8};

        assertEquals(3, StatisticalUtils.average(array1));
        assertEquals(-0.5, StatisticalUtils.average(array2));
    }

    @Test
    void median() {
        // Define arrays
        double[] array1 = {
                9, 2, 3, 4, 5,
                6, 5, 6, 7, 8,
                9, 8, 7, 4, 4,
                2, 9, 1, 2, 2,
                1, 2, 2, 3, 3
        };
        double[] array2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] array3 = {1234};
        double[] array4 = {};

        // Assertions
        assertEquals(4, StatisticalUtils.median(array1));
        assertEquals(5.5, StatisticalUtils.median(array2));
        assertEquals(1234, StatisticalUtils.median(array3));
        assertEquals(Double.NaN, StatisticalUtils.median(array4));
    }

    @Test
    void cov() {
        // Test 1
        double[] x1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] y1 = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        double[][] cov1 = StatisticalUtils.cov(x1, y1);

        assertArrayEquals(new double[]{9.16667, 9.16667}, cov1[0], 1e-5);
        assertArrayEquals(new double[]{9.16667, 9.16667}, cov1[1], 1e-5);

        // Test 2
        double[] x2 = {1, -1, 2, -2, 0};
        double[] y2 = {-1, 1, -2, 2, 0};
        double[][] cov2 = StatisticalUtils.cov(x2, y2);

        assertArrayEquals(new double[]{2.5, -2.5}, cov2[0], 1e-5);
        assertArrayEquals(new double[]{-2.5, 2.5}, cov2[1], 1e-5);

        // Test 3
        double[] x3 = {-2, -1, 0, 1, 2};
        double[] y3 = {4, 1, 0, 1, 4};
        double[][] cov3 = StatisticalUtils.cov(x3, y3);

        assertArrayEquals(new double[]{2.5, 0}, cov3[0], 1e-5);
        assertArrayEquals(new double[]{0, 3.5}, cov3[1], 1e-5);

        // Test exceptions
        assertThrowsExactly(LengthException.class, () -> StatisticalUtils.cov(new double[0], y1));
        assertThrowsExactly(LengthException.class, () -> StatisticalUtils.cov(x1, new double[0]));
        assertThrowsExactly(LengthException.class, () -> StatisticalUtils.cov(new double[0], new double[0]));

        assertThrowsExactly(LengthException.class, () -> StatisticalUtils.cov(x1, y2));
        assertThrowsExactly(LengthException.class, () -> StatisticalUtils.cov(x2, y1));
    }

    @Test
    void corrcoef() {
        // Test 1
        double[] x1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] y1 = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        double[][] r1 = StatisticalUtils.corrcoef(x1, y1);

        assertArrayEquals(new double[]{1, 1}, r1[0], 1e-5);
        assertArrayEquals(new double[]{1, 1}, r1[1], 1e-5);

        // Test 2
        double[] x2 = {1, -1, 2, -2, 0};
        double[] y2 = {-1, 1, -2, 2, 0};
        double[][] r2 = StatisticalUtils.corrcoef(x2, y2);

        assertArrayEquals(new double[]{1, -1}, r2[0], 1e-5);
        assertArrayEquals(new double[]{-1, 1}, r2[1], 1e-5);

        // Test 3
        double[] x3 = {-2, -1, 0, 1, 2};
        double[] y3 = {4, 1, 0, 1, 4};
        double[][] r3 = StatisticalUtils.corrcoef(x3, y3);

        assertArrayEquals(new double[]{1, 0}, r3[0], 1e-5);
        assertArrayEquals(new double[]{0, 1}, r3[1], 1e-5);
    }
}