/*
 * ArrayUtilsTest.java
 * Description: Test `ArrayUtils.java`.
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
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.misc.tuples.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ArrayUtilsTest {
    // Element identification methods
    @Test
    void findIndex() {
        // Define the arrays
        Double[] array1 = {1., 2., 3.4, 5.6};
        String[] array2 = {"Hello", "World", "world", "hello", "!"};
        Integer[] array3 = {7, 8, 9};
        Double[] array4 = {0., 0., 0., 1., 1., 2.};

        // Run tests
        assertEquals(0, ArrayUtils.findIndex(array1, 1.));
        assertEquals(1, ArrayUtils.findIndex(array1, 2.));
        assertEquals(2, ArrayUtils.findIndex(array1, 3.4));
        assertEquals(3, ArrayUtils.findIndex(array1, 5.6));

        assertEquals(0, ArrayUtils.findIndex(array2, "Hello"));
        assertEquals(1, ArrayUtils.findIndex(array2, "World"));
        assertEquals(2, ArrayUtils.findIndex(array2, "world"));
        assertEquals(3, ArrayUtils.findIndex(array2, "hello"));
        assertEquals(4, ArrayUtils.findIndex(array2, "!"));

        assertEquals(0, ArrayUtils.findIndex(array3, 7));
        assertEquals(1, ArrayUtils.findIndex(array3, 8));
        assertEquals(2, ArrayUtils.findIndex(array3, 9));

        assertEquals(0, ArrayUtils.findIndex(array4, 0.));
        assertEquals(3, ArrayUtils.findIndex(array4, 1.));
        assertEquals(5, ArrayUtils.findIndex(array4, 2.));

        assertThrowsExactly(NoSuchElementException.class, () -> ArrayUtils.findIndex(array1, 1337.));
        assertThrowsExactly(NoSuchElementException.class, () -> ArrayUtils.findIndex(array2, 1337.));
        assertThrowsExactly(NoSuchElementException.class, () -> ArrayUtils.findIndex(array3, 1337.));
        assertThrowsExactly(NoSuchElementException.class, () -> ArrayUtils.findIndex(array4, 1337.));
    }

    @Test
    void localMaximum() {
        double[] array = {
                9, 2, 3, 4, 5,
                6, 5, 6, 7, 8,
                9, 8, 7, 4, 4,
                2, 9, 1, 2, 2,
                1, 2, 2, 3, 3
        };
        boolean[] correct = {
                false, false, false, false, false,
                true, false, false, false, false,
                true, false, false, false, false,
                false, true, false, true, false,
                false, true, false, true, false
        };

        assertArrayEquals(correct, ArrayUtils.localMaximum(array));
    }

    @Test
    void takeElem() {
        assertArrayEquals(new double[]{2, 4, 5, 8}, ArrayUtils.takeElem(new double[]{1, 2, 3, 4, 5, 6, 7, 8}, new int[]{1, 3, 4, 7}));
    }

    @Test
    void searchSorted() {
        // Define searching arrays
        double[] array1 = new double[]{1.23, 3.45, 5.67, 7.89, 9.01};
        double[] array2 = new double[]{1, 1, 2, 3, 5};
        double[] array3 = new double[]{-10, 10};

        // Run tests
        assertEquals(0, ArrayUtils.searchSorted(array1, 0.12));
        assertEquals(0, ArrayUtils.searchSorted(array1, 1.23));
        assertEquals(2, ArrayUtils.searchSorted(array1, 4.56));
        assertEquals(3, ArrayUtils.searchSorted(array1, 6));
        assertEquals(2, ArrayUtils.searchSorted(array1, 5.67));

        assertEquals(4, ArrayUtils.searchSorted(array2, 4));
        assertEquals(5, ArrayUtils.searchSorted(array2, 6));
        assertEquals(0, ArrayUtils.searchSorted(array2, 0));
        assertEquals(0, ArrayUtils.searchSorted(array2, 1));

        assertEquals(0, ArrayUtils.searchSorted(array3, -11));
        assertEquals(0, ArrayUtils.searchSorted(array3, -10));
        assertEquals(1, ArrayUtils.searchSorted(array3, -9));
        assertEquals(1, ArrayUtils.searchSorted(array3, 0));
        assertEquals(1, ArrayUtils.searchSorted(array3, 9));
        assertEquals(1, ArrayUtils.searchSorted(array3, 10));
        assertEquals(2, ArrayUtils.searchSorted(array3, 11));
    }

    // Array generation methods
    @Test
    void linspace() {
        // Define correct arrays
        double[] correct1 = {
                -0.5, -0.4, -0.3, -0.2, -0.1, 0., 0.1, 0.2, 0.3, 0.4, 0.5
        };
        double[] correct2 = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        double[] correct3 = {
                0., 10d / 3, 20d / 3, 10
        };
        double[] correct4 = {
                -1234
        };
        double[] correct5 = {};

        // Assertions
        assertArrayEquals(correct1, ArrayUtils.linspace(-0.5, 0.5, 11), 1e-5);
        assertArrayEquals(correct2, ArrayUtils.linspace(0, 10, 11), 1e-5);
        assertArrayEquals(correct3, ArrayUtils.linspace(0, 10, 4), 1e-5);
        assertArrayEquals(correct4, ArrayUtils.linspace(-1234, 5678, 1), 1e-5);
        assertArrayEquals(correct5, ArrayUtils.linspace(-1234, 5678, 0), 1e-5);
    }

    @Test
    void histogram() {
        // Test 1
        double[] data1 = {1.5, 2.5, 4.5, 5, 1};
        Pair<Integer[], Double[]> countsAndBins1 = ArrayUtils.histogram(data1, 1, 5, 4);
        Integer[] counts1 = countsAndBins1.value0();
        Double[] bins1 = countsAndBins1.value1();

        assertArrayEquals(new double[]{1, 2, 3, 4, 5}, TypeConversionUtils.toDoubleArray(bins1), 1e-5);
        assertArrayEquals(new int[]{2, 1, 0, 2}, TypeConversionUtils.toIntegerArray(counts1));

        // Test 2
        double[] data2 = {-10, 0, 0.1, -0.2, 0.3, -0.4, 0.5, -0.6, 0.7, -0.8, 0.9, -1, 1, 0, 0, 0, 0, 0, 10};
        Pair<Integer[], Double[]> countsAndBins2 = ArrayUtils.histogram(data2, -1, 1, 10);
        Integer[] counts2 = countsAndBins2.value0();
        Double[] bins2 = countsAndBins2.value1();

        assertArrayEquals(new double[]{-1, -0.8, -0.6, -0.4, -0.2, 0, 0.2, 0.4, 0.6, 0.8, 1}, TypeConversionUtils.toDoubleArray(bins2), 1e-5);
        assertArrayEquals(new int[]{2, 1, 1, 1, 6, 1, 1, 1, 1, 2}, TypeConversionUtils.toIntegerArray(counts2));

        // Test 3
        double[] data3 = {};
        Pair<Integer[], Double[]> countsAndBins3 = ArrayUtils.histogram(data3, 1, 5, 4);
        Integer[] counts3 = countsAndBins3.value0();
        Double[] bins3 = countsAndBins3.value1();

        assertArrayEquals(new double[]{1, 2, 3, 4, 5}, TypeConversionUtils.toDoubleArray(bins3), 1e-5);
        assertArrayEquals(new int[]{0, 0, 0, 0}, TypeConversionUtils.toIntegerArray(counts3));
    }

    @Test
    void frame() {
        // Perfect length array framing
        double[] array1 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        double[][] framedHorizontal1 = ArrayUtils.frame(array1, 3, 2, false);
        double[][] framedVertical1 = ArrayUtils.frame(array1, 3, 2, true);

        assertArrayEquals(new double[][]{{1, 2, 3}, {3, 4, 5}, {5, 6, 7}, {7, 8, 9}, {9, 10, 11}}, framedHorizontal1);
        assertArrayEquals(new double[][]{{1, 3, 5, 7, 9}, {2, 4, 6, 8, 10}, {3, 5, 7, 9, 11}}, framedVertical1);

        // Imperfect length array framing
        double[] array2 = new double[]{1, 2, 3, 4, 5, 6};
        double[][] framedHorizontal2 = ArrayUtils.frame(array2, 3, 2, false);
        double[][] framedVertical2 = ArrayUtils.frame(array2, 3, 2, true);

        assertArrayEquals(new double[][]{{1, 2, 3}, {3, 4, 5}}, framedHorizontal2);
        assertArrayEquals(new double[][]{{1, 3}, {2, 4}, {3, 5}}, framedVertical2);

        // (Another) Imperfect length array framing
        double[] array3 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[][] framedHorizontal3 = ArrayUtils.frame(array3, 9, 3, false);
        double[][] framedVertical3 = ArrayUtils.frame(array3, 9, 3, true);

        assertArrayEquals(new double[][]{{1, 2, 3, 4, 5, 6, 7, 8, 9}, {4, 5, 6, 7, 8, 9, 10, 11, 12}, {7, 8, 9, 10, 11, 12, 13, 14, 15}, {10, 11, 12, 13, 14, 15, 16, 17, 18}}, framedHorizontal3);
        assertArrayEquals(new double[][]{{1, 4, 7, 10}, {2, 5, 8, 11}, {3, 6, 9, 12}, {4, 7, 10, 13}, {5, 8, 11, 14}, {6, 9, 12, 15}, {7, 10, 13, 16}, {8, 11, 14, 17}, {9, 12, 15, 18}}, framedVertical3);
    }

    @Test
    void maximumFilter1D() {
        assertArrayEquals(new double[]{1, 0, 2, 6, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 1));
        assertArrayEquals(new double[]{1, 1, 2, 6, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 2));
        assertArrayEquals(new double[]{1, 2, 6, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 3));
        assertArrayEquals(new double[]{1, 2, 6, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 4));
        assertArrayEquals(new double[]{2, 6, 9, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 5));
        assertArrayEquals(new double[]{2, 6, 9, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 6));
        assertArrayEquals(new double[]{6, 9, 9, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 7));
        assertArrayEquals(new double[]{6, 9, 9, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 8));
        assertArrayEquals(new double[]{9, 9, 9, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 9));
        assertArrayEquals(new double[]{9, 9, 9, 9, 9}, ArrayUtils.maximumFilter1D(new double[]{1, 0, 2, 6, 9}, 10));

        assertArrayEquals(new double[]{-1, -2, 3, 4, -5, -6}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 1));
        assertArrayEquals(new double[]{-1, -1, 3, 4, 4, -5}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 2));
        assertArrayEquals(new double[]{-1, 3, 4, 4, 4, -5}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 3));
        assertArrayEquals(new double[]{-1, 3, 4, 4, 4, 4}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 4));
        assertArrayEquals(new double[]{3, 4, 4, 4, 4, 4}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 5));
        assertArrayEquals(new double[]{3, 4, 4, 4, 4, 4}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 6));
        assertArrayEquals(new double[]{4, 4, 4, 4, 4, 4}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 7));
        assertArrayEquals(new double[]{4, 4, 4, 4, 4, 4}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 8));
        assertArrayEquals(new double[]{4, 4, 4, 4, 4, 4}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 9));
        assertArrayEquals(new double[]{4, 4, 4, 4, 4, 4}, ArrayUtils.maximumFilter1D(new double[]{-1, -2, 3, 4, -5, -6}, 10));

        assertArrayEquals(new double[]{9, 1, 2, -3, 10, 5, -6}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 1));
        assertArrayEquals(new double[]{9, 9, 2, 2, 10, 10, 5}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 2));
        assertArrayEquals(new double[]{9, 9, 2, 10, 10, 10, 5}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 3));
        assertArrayEquals(new double[]{9, 9, 9, 10, 10, 10, 10}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 4));
        assertArrayEquals(new double[]{9, 9, 10, 10, 10, 10, 10}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 5));
        assertArrayEquals(new double[]{9, 9, 10, 10, 10, 10, 10}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 6));
        assertArrayEquals(new double[]{9, 10, 10, 10, 10, 10, 10}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 7));
        assertArrayEquals(new double[]{9, 10, 10, 10, 10, 10, 10}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 8));
        assertArrayEquals(new double[]{10, 10, 10, 10, 10, 10, 10}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 9));
        assertArrayEquals(new double[]{10, 10, 10, 10, 10, 10, 10}, ArrayUtils.maximumFilter1D(new double[]{9, 1, 2, -3, 10, 5, -6}, 10));
    }

    @Test
    void tile() {
        // Define the array to tile
        double[][] array = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8}
        };

        // Define the correct tiled array
        double[][] correct = new double[][]{
                {1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4},
                {5, 6, 7, 8, 5, 6, 7, 8, 5, 6, 7, 8}
        };

        // Assertions
        assertArrayEquals(correct, ArrayUtils.tile(array, 3));
    }

    // Array modification methods
    @Test
    void lpNormalize() {
        // Define the arrays
        double[] array1 = {1, 2, 4, 8, 16};
        double[] array2 = {1, 3, 9, 27, 81, 243};
        double[] array3 = {0, 1, 0, -2, 0, 3, 0};
        double[] array4 = {0.1, -0.02, 0.003, -0.0004, 0.05};
        double[] array5 = {219, 0.24, 9.14};
        double[] array6 = {0, 0, 0};
        Complex[] array7 = {
                new Complex(1), new Complex(0, 1), new Complex(-1),
                new Complex(0, -1), new Complex(-1, 1)
        };

        // Define correct results
        double[] array1Normalized = {0.0625, 0.125, 0.25, 0.5, 1.};
        double[] array2Normalized = {0.00411183, 0.0123355, 0.0370065, 0.11101951, 0.33305853, 0.9991756};
        double[] array3Normalized = {0., 0.33333333, 0., -0.66666667, 0., 1., 0.};
        double[] array4Normalized = {250., -50., 7.5, -1., 125};
        double[] array5Normalized = {219, 0.24, 9.14};
        double[] array6Normalized = {Double.NaN, Double.NaN, Double.NaN};
        Complex[] array7Normalized = {
                new Complex(0.70710678), new Complex(0, 0.70710678),
                new Complex(-0.70710678), new Complex(0, -0.70710678),
                new Complex(-0.70710678, 0.70710678)
        };

        // Run tests
        assertArrayEquals(array1Normalized, ArrayUtils.lpNormalize(array1, Double.POSITIVE_INFINITY), 1e-4);
        assertArrayEquals(array2Normalized, ArrayUtils.lpNormalize(array2, 5), 1e-4);
        assertArrayEquals(array3Normalized, ArrayUtils.lpNormalize(array3, 0), 1e-4);
        assertArrayEquals(array4Normalized, ArrayUtils.lpNormalize(array4, Double.NEGATIVE_INFINITY), 1e-4);
        assertArrayEquals(array5Normalized, ArrayUtils.lpNormalize(array5, -1337), 1e-4);  // No change
        assertArrayEquals(array6Normalized, ArrayUtils.lpNormalize(array6, 0), 1e-4);

        Complex[] array7Computed = ArrayUtils.lpNormalize(array7, Double.POSITIVE_INFINITY);
        assertEquals(array7Computed.length, array7Normalized.length);
        for (int i = 0; i < array7Normalized.length; i++) {
            assertEquals(array7Normalized[i].roundNicely(4), array7Computed[i].roundNicely(4));
        }
    }

    @Test
    void fixLength() {
        // Define the array for testing
        double[] array = {1, 2, 3, 4, 5};

        // Run tests
        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 0, 0, 0}, ArrayUtils.fixLength(array, 8));
        assertArrayEquals(array, ArrayUtils.fixLength(array, 5));
        assertArrayEquals(new double[]{1, 2, 3}, ArrayUtils.fixLength(array, 3));

        assertThrowsExactly(ValueException.class, () -> ArrayUtils.fixLength(array, 0));
        assertThrowsExactly(ValueException.class, () -> ArrayUtils.fixLength(array, -12));
    }

    @Test
    void padCenterDouble() {
        // Define the arrays
        double[] array1 = {1, 2, 3, 4};
        double[] array2 = {1, 2, 3};
        double[] array3 = {5, 6};
        double[] array4 = {7};

        // Define the correct arrays
        double[] array1Correct = new double[]{0, 0, 1, 2, 3, 4, 0, 0};
        double[] array2Correct = new double[]{0, 0, 0, 1, 2, 3, 0, 0, 0};
        double[] array3Correct = new double[]{0, 0, 0, 0, 5, 6, 0, 0, 0, 0};
        double[] array4Correct = new double[]{0, 0, 0, 7, 0, 0, 0};

        // Run tests
        assertArrayEquals(array1Correct, ArrayUtils.padCenter(array1, 8));
        assertArrayEquals(array2Correct, ArrayUtils.padCenter(array2, 9));
        assertArrayEquals(array3Correct, ArrayUtils.padCenter(array3, 10));
        assertArrayEquals(array4Correct, ArrayUtils.padCenter(array4, 7));

        assertThrowsExactly(ValueException.class, () -> ArrayUtils.padCenter(array1, 3));
    }

    @Test
    void padCenterComplex() {
        // Define the arrays
        Complex[] array1 = {new Complex(1), new Complex(2), new Complex(3), new Complex(4)};
        Complex[] array2 = {new Complex(1), new Complex(2), new Complex(3)};
        Complex[] array3 = {new Complex(5), new Complex(6)};
        Complex[] array4 = {new Complex(7)};

        // Define the correct arrays
        Complex[] array1Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO,
                new Complex(1), new Complex(2), new Complex(3), new Complex(4),
                Complex.ZERO, Complex.ZERO
        };
        Complex[] array2Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO, Complex.ZERO,
                new Complex(1), new Complex(2), new Complex(3),
                Complex.ZERO, Complex.ZERO, Complex.ZERO
        };
        Complex[] array3Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO,
                new Complex(5), new Complex(6),
                Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO
        };
        Complex[] array4Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO, Complex.ZERO,
                new Complex(7),
                Complex.ZERO, Complex.ZERO, Complex.ZERO
        };

        // Run tests
        assertArrayEquals(array1Correct, ArrayUtils.padCenter(array1, 8));
        assertArrayEquals(array2Correct, ArrayUtils.padCenter(array2, 9));
        assertArrayEquals(array3Correct, ArrayUtils.padCenter(array3, 10));
        assertArrayEquals(array4Correct, ArrayUtils.padCenter(array4, 7));

        assertThrowsExactly(ValueException.class, () -> ArrayUtils.padCenter(array1, 3));
    }

    @Test
    void padCenterReflect() {
        // Define the arrays
        double[] array1 = {1, 2, 3, 4};
        double[] array2 = {1, 2, 3};
        double[] array3 = {5, 6};
        double[] array4 = {7, 8};
        double[] array5 = {9};

        // Run tests
        assertArrayEquals(new double[]{2, 1, 1, 2, 3, 4, 4, 3}, ArrayUtils.padCenterReflect(array1, 8));
        assertArrayEquals(new double[]{3, 2, 1, 1, 2, 3, 3, 2, 1}, ArrayUtils.padCenterReflect(array2, 9));
        assertArrayEquals(new double[]{5, 6, 6, 5, 5, 6, 6, 5, 5, 6}, ArrayUtils.padCenterReflect(array3, 10));
        assertArrayEquals(new double[]{8, 7, 7, 8, 8, 7, 7, 8, 8, 7, 7, 8, 8, 7}, ArrayUtils.padCenterReflect(array4, 14));
        assertArrayEquals(new double[]{9, 9, 9, 9, 9, 9, 9}, ArrayUtils.padCenterReflect(array5, 7));

        assertThrowsExactly(ValueException.class, () -> ArrayUtils.padCenterReflect(array1, 0));  // Size is zero
        assertThrowsExactly(ValueException.class, () -> ArrayUtils.padCenterReflect(array2, -1));  // Size is negative
    }

    @Test
    void roll() {
        // Define the array to roll
        double[][] array = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9},
                {10, 11, 12}
        };

        // Define correct output arrays
        double[][] correct1 = {
                {7, 8, 9},
                {10, 11, 12},
                {1, 2, 3},
                {4, 5, 6},
        };
        double[][] correct2 = {
                {3, 1, 2},
                {6, 4, 5},
                {9, 7, 8},
                {12, 10, 11}
        };

        // Assertions
        assertArrayEquals(correct1, ArrayUtils.roll(array, 2, 0));
        assertArrayEquals(correct1, ArrayUtils.roll(array, 6, 0));
        assertArrayEquals(correct1, ArrayUtils.roll(array, -2, 0));
        assertArrayEquals(correct1, ArrayUtils.roll(array, -6, 0));
        assertArrayEquals(correct2, ArrayUtils.roll(array, 1, 1));
        assertArrayEquals(correct2, ArrayUtils.roll(array, 4, 1));
        assertArrayEquals(correct2, ArrayUtils.roll(array, -2, 1));
        assertArrayEquals(correct2, ArrayUtils.roll(array, -5, 1));
    }

    // Fundamental matrix methods
    @Test
    void transpose() {
        // Define the arrays and their transposes
        double[][] A = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}
        };
        Complex[][] B = new Complex[][]{
                {new Complex(1, 1), new Complex(2, 2), new Complex(3, 3)},
                {new Complex(4, 4), new Complex(5, 5), new Complex(6, 6)}
        };
        boolean[][] C = new boolean[][]{
                {false, false, false, true},
                {false, true, true, false},
                {true, false, true, false}
        };

        double[][] At = new double[][]{
                {1, 5, 9},
                {2, 6, 10},
                {3, 7, 11},
                {4, 8, 12}
        };
        Complex[][] Bt = new Complex[][]{
                {new Complex(1, 1), new Complex(4, 4)},
                {new Complex(2, 2), new Complex(5, 5)},
                {new Complex(3, 3), new Complex(6, 6)}
        };
        boolean[][] Ct = new boolean[][]{
                {false, false, true},
                {false, true, false},
                {false, true, true},
                {true, false, false}
        };

        // Run tests
        assertArrayEquals(At, ArrayUtils.transpose(A));
        assertArrayEquals(Bt, ArrayUtils.transpose(B));
        assertArrayEquals(Ct, ArrayUtils.transpose(C));
    }

    @Test
    void matadd() {
        // Define matrices
        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        double[][] B = {{7, 8, 9}, {10, 11, 12}};

        // Define expected output
        double[][] expectedAB = {{8, 10, 12}, {14, 16, 18}};

        // Compute matrix sum
        double[][] resultAB = ArrayUtils.matadd(A, B);

        // Assertion
        assertArrayEquals(expectedAB, resultAB);

        // Assert exceptions
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matadd(new double[3][4], new double[4][4]));
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matadd(new double[3][4], new double[3][5]));
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matadd(new double[3][4], new double[4][5]));
    }

    @Test
    void matsub() {
        // Define matrices
        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        double[][] B = {{7, 8, 9}, {10, 11, 12}};

        // Define expected outputs
        double[][] expectedAB = {{-6, -6, -6}, {-6, -6, -6}};

        // Compute matrix sums
        double[][] resultAB = ArrayUtils.matsub(A, B);

        // Assertion
        assertArrayEquals(expectedAB, resultAB);

        // Assert exceptions
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matsub(new double[3][4], new double[4][4]));
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matsub(new double[3][4], new double[3][5]));
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matsub(new double[3][4], new double[4][5]));
    }

    @Test
    void matmulComplex() {
        // Define matrices
        Complex[][] A = new Complex[][]{
                {new Complex(1, 0), new Complex(0, -1)},
                {new Complex(0, 1), new Complex(-1, 0)}
        };
        Complex[][] B = new Complex[][]{
                {new Complex(1, 0), new Complex(1, 1), new Complex(0, 1)},
                {new Complex(0, 1), new Complex(1, 1), new Complex(1, 0)}
        };
        Complex[][] C = new Complex[][]{
                {new Complex(1, 0), new Complex(1, 0)},
                {new Complex(0, 2), new Complex(0, 2)},
                {new Complex(-3, 0), new Complex(-3, 0)}
        };

        // Expected matrix multiplication results
        Complex[][] X = new Complex[][]{
                {new Complex(2, 0), new Complex(0, 0)},
                {new Complex(0, 0), new Complex(2, 0)}
        };
        Complex[][] Y = new Complex[][]{
                {new Complex(-1, -1), new Complex(-1, -1)},
                {new Complex(-5, 3), new Complex(-5, 3)}
        };
        Complex[][] Z = new Complex[][]{
                {new Complex(1, 1), new Complex(2, 2), new Complex(1, 1)},
                {new Complex(-2, 2), new Complex(-4, 4), new Complex(-2, 2)},
                {new Complex(-3, -3), new Complex(-6, -6), new Complex(-3, -3)}
        };

        // Actual matrix multiplication results
        Complex[][] AA = ArrayUtils.matmul(A, A);
        Complex[][] BC = ArrayUtils.matmul(B, C);
        Complex[][] CB = ArrayUtils.matmul(C, B);

        // Assertions
        assertArrayEquals(X, AA);
        assertArrayEquals(Y, BC);
        assertArrayEquals(Z, CB);

        assertDoesNotThrow(() -> ArrayUtils.matmul(A, B));                          // Should execute fine...
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matmul(B, A));  // ...but not this
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matmul(A, C));  // Should not execute fine...
        assertDoesNotThrow(() -> ArrayUtils.matmul(C, A));                          // ...but this should
    }

    @Test
    void matmulDouble() {
        // Define matrices
        double[][] A = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        double[][] B = {
                {10, 11, 12},
                {13, 14, 15}
        };
        double[][] C = {
                {0.1, 0.2},
                {0.3, 0.4},
                {0.5, 0.6}
        };

        // Expected matrix multiplication results
        double[][] X = {
                {30, 36, 42},
                {66, 81, 96},
                {102, 126, 150}
        };
        double[][] Y = {
                {10.3, 13.6},
                {13, 17.2}
        };
        double[][] Z = {
                {3.6, 3.9, 4.2},
                {8.2, 8.9, 9.6},
                {12.8, 13.9, 15}
        };

        // Actual matrix multiplication results
        double[][] AA = ArrayUtils.matmul(A, A);
        double[][] BC = ArrayUtils.matmul(B, C);
        double[][] CB = ArrayUtils.matmul(C, B);

        // Assertions
        assertArrayEquals(X[0], AA[0], 0.001);
        assertArrayEquals(X[1], AA[1], 0.001);
        assertArrayEquals(X[2], AA[2], 0.001);

        assertArrayEquals(Y[0], BC[0], 0.001);
        assertArrayEquals(Y[1], BC[1], 0.001);

        assertArrayEquals(Z[0], CB[0], 0.001);
        assertArrayEquals(Z[1], CB[1], 0.001);
        assertArrayEquals(Z[2], CB[2], 0.001);

        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matmul(A, B));  // Should not execute fine...
        assertDoesNotThrow(() -> ArrayUtils.matmul(B, A));                          // ...but this should
        assertDoesNotThrow(() -> ArrayUtils.matmul(A, C));                          // Should execute fine...
        assertThrowsExactly(LengthException.class, () -> ArrayUtils.matmul(C, A));  // ...but not this
    }

    @Test
    void matmulCheckConditions() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Test constants
        int leafSize = 2;
        final Random random = new Random(11235);

        // Make the private matrix multiplication methods available to the test
        Method matmulIJK = ArrayUtils.class.getDeclaredMethod("matmulIJK", double[][].class, double[][].class);
        Method matmulStrassen = ArrayUtils.class.getDeclaredMethod(
                "matmulStrassen", double[][].class, double[][].class, int.class
        );

        matmulIJK.setAccessible(true);
        matmulStrassen.setAccessible(true);

        // Test all possible 'small' matrix sizes
        int[] numARowsVals = {1, 2, 3};
        int[] numCommonVals = {1, 2, 3};
        int[] numBColsVals = {1, 2, 3};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(3, 3);  // 3 different sets of numbers
        for (int[] indices : indexProduct) {
            // Get the lengths of the matrices
            int numARows = numARowsVals[indices[0]];
            int numCommon = numCommonVals[indices[1]];
            int numBCols = numBColsVals[indices[2]];

            // Initialize matrices
            double[][] A = new double[numARows][numCommon];
            double[][] B = new double[numCommon][numBCols];

            for (int i = 0; i < numARows; i++) {
                for (int j = 0; j < numCommon; j++) {
                    A[i][j] = random.nextDouble(-1e3, 1e3);
                }
            }

            for (int i = 0; i < numCommon; i++) {
                for (int j = 0; j < numBCols; j++) {
                    B[i][j] = random.nextDouble(-1e3, 1e3);
                }
            }

            // Compute matrix multiplications
            double[][] ijkResult = (double[][]) matmulIJK.invoke(null, A, B);
            double[][] matmulNormalResult = ArrayUtils.matmul(A, B, leafSize);
            double[][] matmulStrassenResult = (double[][]) matmulStrassen.invoke(null, A, B, leafSize);

            // Check if equal
            for (int i = 0; i < numARows; i++) {
                assertArrayEquals(ijkResult[i], matmulNormalResult[i], 1e-5);
                assertArrayEquals(ijkResult[i], matmulStrassenResult[i], 1e-5);
            }
        }
    }

    @Test
    void matmulComplexStrassenCheck() {
        // Test constants
        final int matrixSize = 256;
        final int leafSize = 64;

        final Random random = new Random(67890);

        // Initialize matrices
        Complex[][] A = new Complex[matrixSize][matrixSize];
        Complex[][] B = new Complex[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                A[i][j] = new Complex(random.nextDouble(-1e3, 1e3), random.nextDouble(-1e3, 1e3));
                B[i][j] = new Complex(random.nextDouble(-1e3, 1e3), random.nextDouble(-1e3, 1e3));
            }
        }

        // Compute matrix multiplications
        Complex[][] ijkResult = complexMatmulIJK(A, B);
        Complex[][] strassenResult = ArrayUtils.matmul(A, B, leafSize);

        // Check if equal
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                assertEquals(ijkResult[i][j].roundNicely(3), strassenResult[i][j].roundNicely(3));
            }
        }
    }

    @Test
    void matmulDoubleStrassenCheck() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Test constants
        final int matrixSize = 256;
        final int leafSize = 64;

        final Random random = new Random(12345);

        // Make the IJK multiplication method available to the test
        Method matmulIJK = ArrayUtils.class.getDeclaredMethod("matmulIJK", double[][].class, double[][].class);
        matmulIJK.setAccessible(true);

        // Initialize matrices
        double[][] A = new double[matrixSize][matrixSize];
        double[][] B = new double[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                A[i][j] = random.nextDouble(-1e3, 1e3);
                B[i][j] = random.nextDouble(-1e3, 1e3);
            }
        }

        // Compute matrix multiplications
        double[][] ijkResult = (double[][]) matmulIJK.invoke(null, A, B);
        double[][] strassenResult = ArrayUtils.matmul(A, B, leafSize);

        // Check if equal
        for (int i = 0; i < matrixSize; i++) {
            assertArrayEquals(ijkResult[i], strassenResult[i], 1e-5);
        }
    }

    @Test
    void flatten() {
        // Define test arrays
        Integer[][] array1 = {
                {1, 2, 3, 4},
                {5, 6, 7},
                {8, 9},
                {10}
        };
        Double[][][] array2 = {
                {{1d}, {2d, 3d}},
                {{4d, 5d, 6d}},
                {{7d}, {8d, 9d}, {10d}},
        };

        // Define correct arrays
        Integer[] correct1 = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        Double[] correct2 = {
                1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d
        };

        // Run tests
        assertArrayEquals(correct1, ArrayUtils.flatten(array1, Integer.class).toArray());
        assertArrayEquals(correct2, ArrayUtils.flatten(array2, Double.class).toArray());
        assertNull(ArrayUtils.flatten(null, String.class));
    }

    // Helper functions
    static Complex[][] complexMatmulIJK(Complex[][] A, Complex[][] B) {
        // Lengths
        int numRowsA = A.length;
        int numCommon = A[0].length;
        int numColsB = B[0].length;

        // Multiplication
        Complex[][] C = new Complex[numRowsA][numColsB];

        for (int i = 0; i < numRowsA; i++) {
            for (int j = 0; j < numColsB; j++) {
                Complex currElem = Complex.ZERO;
                for (int k = 0; k < numCommon; k++) {
                    currElem = currElem.plus(A[i][k].times(B[k][j]));
                }
                C[i][j] = currElem;
            }
        }

        return C;
    }
}