package app.auditranscribe.utils;

import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.misc.Complex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MatrixUtilsTest {
    // Fundamental operations
    @Test
    void matadd() {
        // Define matrices
        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        double[][] B = {{7, 8, 9}, {10, 11, 12}};

        // Define expected output
        double[][] expectedAB = {{8, 10, 12}, {14, 16, 18}};

        // Compute matrix sum
        double[][] resultAB = MatrixUtils.matadd(A, B);

        // Assertion
        assertArrayEquals(expectedAB, resultAB);

        // Assert exceptions
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matadd(new double[3][4], new double[4][4]));
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matadd(new double[3][4], new double[3][5]));
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matadd(new double[3][4], new double[4][5]));
    }

    @Test
    void matsub() {
        // Define matrices
        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        double[][] B = {{7, 8, 9}, {10, 11, 12}};

        // Define expected outputs
        double[][] expectedAB = {{-6, -6, -6}, {-6, -6, -6}};

        // Compute matrix sums
        double[][] resultAB = MatrixUtils.matsub(A, B);

        // Assertion
        assertArrayEquals(expectedAB, resultAB);

        // Assert exceptions
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matsub(new double[3][4], new double[4][4]));
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matsub(new double[3][4], new double[3][5]));
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matsub(new double[3][4], new double[4][5]));
    }

    @Test
    void matmul_double() {
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
        double[][] AA = MatrixUtils.matmul(A, A);
        double[][] BC = MatrixUtils.matmul(B, C);
        double[][] CB = MatrixUtils.matmul(C, B);

        // Assertions
        assertArrayEquals(X[0], AA[0], 0.001);
        assertArrayEquals(X[1], AA[1], 0.001);
        assertArrayEquals(X[2], AA[2], 0.001);

        assertArrayEquals(Y[0], BC[0], 0.001);
        assertArrayEquals(Y[1], BC[1], 0.001);

        assertArrayEquals(Z[0], CB[0], 0.001);
        assertArrayEquals(Z[1], CB[1], 0.001);
        assertArrayEquals(Z[2], CB[2], 0.001);

        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matmul(A, B));  // Should NOT execute fine...
        assertDoesNotThrow(() -> MatrixUtils.matmul(B, A));                          // ...but this should
        assertDoesNotThrow(() -> MatrixUtils.matmul(A, C));                          // Should execute fine...
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matmul(C, A));  // ...but not this
    }

    @Test
    void matmul_complex() {
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
        Complex[][] AA = MatrixUtils.matmul(A, A);
        Complex[][] BC = MatrixUtils.matmul(B, C);
        Complex[][] CB = MatrixUtils.matmul(C, B);

        // Assertions
        assertArrayEquals(X, AA);
        assertArrayEquals(Y, BC);
        assertArrayEquals(Z, CB);

        Assertions.assertDoesNotThrow(() -> MatrixUtils.matmul(A, B));               // Should execute fine...
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matmul(B, A));  // ...but not this
        assertThrowsExactly(LengthException.class, () -> MatrixUtils.matmul(A, C));  // Should NOT execute fine...
        Assertions.assertDoesNotThrow(() -> MatrixUtils.matmul(C, A));               // ...but this should
    }

    @Test
    void matmul_checkConditions() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Test constants
        int leafSize = 2;
        final Random random = new Random(11235);

        // Make the private matrix multiplication methods available to the test
        Method matmulIJK = MatrixUtils.class.getDeclaredMethod("matmulIJK", double[][].class, double[][].class);
        Method matmulStrassen = MatrixUtils.class.getDeclaredMethod(
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
            double[][] matmulNormalResult = MatrixUtils.matmul(A, B, leafSize);
            double[][] matmulStrassenResult = (double[][]) matmulStrassen.invoke(null, A, B, leafSize);

            // Check if equal
            for (int i = 0; i < numARows; i++) {
                assertArrayEquals(ijkResult[i], matmulNormalResult[i], 1e-5);
                assertArrayEquals(ijkResult[i], matmulStrassenResult[i], 1e-5);
            }
        }
    }

    @Test
    void matmul_complexStrassenCheck() {
        // Test constants
        final int matrixSize = 128;
        final int leafSize = 64;

        final Random random = new Random(67890);

        // Initialize matrices
        Complex[][] A = new Complex[matrixSize][matrixSize];
        Complex[][] B = new Complex[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                A[i][j] = new Complex(
                        random.nextDouble(-1e3, 1e3),
                        random.nextDouble(-1e3, 1e3)
                );
                B[i][j] = new Complex(
                        random.nextDouble(-1e3, 1e3),
                        random.nextDouble(-1e3, 1e3)
                );
            }
        }

        // Compute matrix multiplications
        Complex[][] ijkResult = complexMatmulIJK(A, B);
        Complex[][] strassenResult = MatrixUtils.matmul(A, B, leafSize);

        // Check if equal
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                assertEquals(ijkResult[i][j].roundNicely(3), strassenResult[i][j].roundNicely(3));
            }
        }
    }

    @Test
    void matmul_doubleStrassenCheck() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Test constants
        final int matrixSize = 256;
        final int leafSize = 64;

        final Random random = new Random(12345);

        // Make the IJK multiplication method available to the test
        Method matmulIJK = MatrixUtils.class.getDeclaredMethod("matmulIJK", double[][].class, double[][].class);
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
        double[][] strassenResult = MatrixUtils.matmul(A, B, leafSize);

        // Check if equal
        for (int i = 0; i < matrixSize; i++) {
            assertArrayEquals(ijkResult[i], strassenResult[i], 1e-5);
        }
    }

    // Matrix modification methods
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
        assertArrayEquals(At, MatrixUtils.transpose(A));
        assertArrayEquals(Bt, MatrixUtils.transpose(B));
        assertArrayEquals(Ct, MatrixUtils.transpose(C));
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