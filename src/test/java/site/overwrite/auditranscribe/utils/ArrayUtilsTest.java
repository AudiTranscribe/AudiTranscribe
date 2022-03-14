/*
 * ArrayUtilsTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-03-14
 *
 * Description: Test `ArrayUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayUtilsTest {
    @Test
    void searchSorted() {
        // Define searching arrays
        double[] array1 = new double[]{1.23, 3.45, 5.67, 7.89, 9.01};
        double[] array2 = new double[]{1, 1, 2, 3, 5};

        // Run tests
        assertEquals(0, ArrayUtils.searchSorted(array1, 0.12));
        assertEquals(2, ArrayUtils.searchSorted(array1, 4.56));
        assertEquals(3, ArrayUtils.searchSorted(array1, 6));
        assertEquals(2, ArrayUtils.searchSorted(array1, 5.67));

        assertEquals(4, ArrayUtils.searchSorted(array2, 4));
        assertEquals(5, ArrayUtils.searchSorted(array2, 6));
        assertEquals(0, ArrayUtils.searchSorted(array2, 0));
        assertEquals(0, ArrayUtils.searchSorted(array2, 1));
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
    void transpose() {
        // Define the two arrays and their transposes
        double[][] A = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}
        };
        Complex[][] B = new Complex[][]{
                {new Complex(1, 1), new Complex(2, 2), new Complex(3, 3)},
                {new Complex(4, 4), new Complex(5, 5), new Complex(6, 6)}
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

        // Run tests
        assertArrayEquals(At, ArrayUtils.transpose(A));
        assertArrayEquals(Bt, ArrayUtils.transpose(B));
    }

    @Test
    void matmul() {
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

        // Matrix multiplication results
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

        // Assertions
        assertArrayEquals(X, ArrayUtils.matmul(A, A));
        assertArrayEquals(Y, ArrayUtils.matmul(B, C));
        assertArrayEquals(Z, ArrayUtils.matmul(C, B));
    }
}