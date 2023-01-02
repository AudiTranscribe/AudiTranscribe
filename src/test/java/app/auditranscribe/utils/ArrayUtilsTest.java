package app.auditranscribe.utils;

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.misc.Complex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayUtilsTest {
    // Searching methods
    @Test
    void findLocalMaxima() {
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

        assertArrayEquals(correct, ArrayUtils.findLocalMaxima(array));
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
    void frameHorizontal() {
        // Perfect length array framing
        double[] array1 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        double[][] framedHorizontal1 = ArrayUtils.frameHorizontal(array1, 3, 2);
        assertArrayEquals(new double[][]{{1, 2, 3}, {3, 4, 5}, {5, 6, 7}, {7, 8, 9}, {9, 10, 11}}, framedHorizontal1);

        // Imperfect length array framing
        double[] array2 = new double[]{1, 2, 3, 4, 5, 6};
        double[][] framedHorizontal2 = ArrayUtils.frameHorizontal(array2, 3, 2);
        assertArrayEquals(new double[][]{{1, 2, 3}, {3, 4, 5}}, framedHorizontal2);

        // (Another) Imperfect length array framing
        double[] array3 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[][] framedHorizontal3 = ArrayUtils.frameHorizontal(array3, 9, 3);
        assertArrayEquals(
                new double[][]{
                        {1, 2, 3, 4, 5, 6, 7, 8, 9},
                        {4, 5, 6, 7, 8, 9, 10, 11, 12},
                        {7, 8, 9, 10, 11, 12, 13, 14, 15},
                        {10, 11, 12, 13, 14, 15, 16, 17, 18}
                },
                framedHorizontal3
        );
    }

    @Test
    void frameVertical() {
        // Perfect length array framing
        double[] array1 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        double[][] framedVertical1 = ArrayUtils.frameVertical(array1, 3, 2);
        assertArrayEquals(new double[][]{{1, 3, 5, 7, 9}, {2, 4, 6, 8, 10}, {3, 5, 7, 9, 11}}, framedVertical1);

        // Imperfect length array framing
        double[] array2 = new double[]{1, 2, 3, 4, 5, 6};
        double[][] framedVertical2 = ArrayUtils.frameVertical(array2, 3, 2);
        assertArrayEquals(new double[][]{{1, 3}, {2, 4}, {3, 5}}, framedVertical2);

        // (Another) Imperfect length array framing
        double[] array3 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[][] framedVertical3 = ArrayUtils.frameVertical(array3, 9, 3);
        assertArrayEquals(
                new double[][]{
                        {1, 4, 7, 10},
                        {2, 5, 8, 11},
                        {3, 6, 9, 12},
                        {4, 7, 10, 13},
                        {5, 8, 11, 14},
                        {6, 9, 12, 15},
                        {7, 10, 13, 16},
                        {8, 11, 14, 17},
                        {9, 12, 15, 18}
                },
                framedVertical3
        );
    }

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
    void padCenter() {
        // Define the arrays
        double[] array1 = {1, 2, 3, 4};
        double[] array2 = {1, 2, 3};
        double[] array3 = {5, 6};
        double[] array4 = {7};

        Complex[] array5 = {new Complex(1), new Complex(2), new Complex(3), new Complex(4)};
        Complex[] array6 = {new Complex(1), new Complex(2), new Complex(3)};
        Complex[] array7 = {new Complex(5), new Complex(6)};
        Complex[] array8 = {new Complex(7)};

        // Define the correct arrays
        double[] array1Correct = new double[]{0, 0, 1, 2, 3, 4, 0, 0};
        double[] array2Correct = new double[]{0, 0, 0, 1, 2, 3, 0, 0, 0};
        double[] array3Correct = new double[]{0, 0, 0, 0, 5, 6, 0, 0, 0, 0};
        double[] array4Correct = new double[]{0, 0, 0, 7, 0, 0, 0};

        Complex[] array5Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO,
                new Complex(1), new Complex(2), new Complex(3), new Complex(4),
                Complex.ZERO, Complex.ZERO
        };
        Complex[] array6Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO, Complex.ZERO,
                new Complex(1), new Complex(2), new Complex(3),
                Complex.ZERO, Complex.ZERO, Complex.ZERO
        };
        Complex[] array7Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO,
                new Complex(5), new Complex(6),
                Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO
        };
        Complex[] array8Correct = new Complex[]{
                Complex.ZERO, Complex.ZERO, Complex.ZERO,
                new Complex(7),
                Complex.ZERO, Complex.ZERO, Complex.ZERO
        };

        // Run tests
        assertArrayEquals(array1Correct, ArrayUtils.padCenter(array1, 8));
        assertArrayEquals(array2Correct, ArrayUtils.padCenter(array2, 9));
        assertArrayEquals(array3Correct, ArrayUtils.padCenter(array3, 10));
        assertArrayEquals(array4Correct, ArrayUtils.padCenter(array4, 7));
        assertArrayEquals(array5Correct, ArrayUtils.padCenter(array5, 8));
        assertArrayEquals(array6Correct, ArrayUtils.padCenter(array6, 9));
        assertArrayEquals(array7Correct, ArrayUtils.padCenter(array7, 10));
        assertArrayEquals(array8Correct, ArrayUtils.padCenter(array8, 7));

        assertThrowsExactly(ValueException.class, () -> ArrayUtils.padCenter(array1, 3));
        assertThrowsExactly(ValueException.class, () -> ArrayUtils.padCenter(array5, 3));
    }

    // Matrix methods
    @Test
    void transpose() {
        // Define the matrices and their transposes
        double[][] A = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}
        };
        boolean[][] B = new boolean[][]{
                {false, false, false, true},
                {false, true, true, false},
                {true, false, true, false}
        };
        Complex[][] C = new Complex[][]{
                {new Complex(1, 1), new Complex(2, 2), new Complex(3, 3)},
                {new Complex(4, 4), new Complex(5, 5), new Complex(6, 6)}
        };

        double[][] At = new double[][]{
                {1, 5, 9},
                {2, 6, 10},
                {3, 7, 11},
                {4, 8, 12}
        };
        boolean[][] Bt = new boolean[][]{
                {false, false, true},
                {false, true, false},
                {false, true, true},
                {true, false, false}
        };
        Complex[][] Ct = new Complex[][]{
                {new Complex(1, 1), new Complex(4, 4)},
                {new Complex(2, 2), new Complex(5, 5)},
                {new Complex(3, 3), new Complex(6, 6)}
        };

        // Run tests
        assertArrayEquals(At, ArrayUtils.transpose(A));
        assertArrayEquals(Bt, ArrayUtils.transpose(B));
        assertArrayEquals(Ct, ArrayUtils.transpose(C));
    }
}