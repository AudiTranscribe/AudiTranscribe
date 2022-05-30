/*
 * ArrayUtilsTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-05-30
 *
 * Description: Test `ArrayUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.misc.Complex;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ArrayUtilsTest {
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
    void padCenter() {
        // Define the arrays
        double[] array1 = {1, 2, 3, 4};
        double[] array2 = {1, 2, 3};
        double[] array3 = {5, 6};
        double[] array4 = {7};

        // Run tests
        assertArrayEquals(new double[]{0, 0, 1, 2, 3, 4, 0, 0}, ArrayUtils.padCenter(array1, 8));
        assertArrayEquals(new double[]{0, 0, 0, 1, 2, 3, 0, 0, 0}, ArrayUtils.padCenter(array2, 9));
        assertArrayEquals(new double[]{0, 0, 0, 0, 5, 6, 0, 0, 0, 0}, ArrayUtils.padCenter(array3, 10));
        assertArrayEquals(new double[]{0, 0, 0, 7, 0, 0, 0}, ArrayUtils.padCenter(array4, 7));
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
    }
}