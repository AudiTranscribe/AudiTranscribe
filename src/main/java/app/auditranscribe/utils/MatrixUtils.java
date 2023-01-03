/*
 * MatrixUtils.java
 * Description: Matrix utilities.
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

package app.auditranscribe.utils;

import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.misc.Complex;

/**
 * Matrix utilities.
 */
public final class MatrixUtils {
    // Constants
    public static final int MATMUL_LEAF_SIZE = 2048;  // Leaf size for matrix multiplication

    private MatrixUtils() {
        // Private constructor to signal this is a utility class
    }

    // Fundamental operations

    /**
     * Add the two real-valued matrices together.
     *
     * @param A The first matrix.
     * @param B The second matrix.
     * @return The sum of the two matrices.
     */
    public static double[][] matadd(double[][] A, double[][] B) {
        // Check if the matrices can be added
        if ((A.length != B.length) || (A[0].length != B[0].length)) {
            throw new LengthException("Matrix sizes not suitable for addition");
        }

        // Perform matrix addition
        int m = A.length;
        int n = A[0].length;

        double[][] output = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                output[i][j] = A[i][j] + B[i][j];
            }
        }

        return output;
    }

    /**
     * Subtract the two real-valued matrices (i.e., <code>A - B</code>).
     *
     * @param A The first matrix.
     * @param B The second matrix.
     * @return The difference (<code>A - B</code>) of the two matrices.
     */
    public static double[][] matsub(double[][] A, double[][] B) {
        // Check if the matrices can be added
        if ((A.length != B.length) || (A[0].length != B[0].length)) {
            throw new LengthException("Matrix sizes not suitable for subtraction");
        }

        // Perform matrix subtraction
        int m = A.length;
        int n = A[0].length;

        double[][] output = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                output[i][j] = A[i][j] - B[i][j];
            }
        }

        return output;
    }

    /**
     * Matrix multiply two real-numbered matrices.<br>
     * Uses default leaf size of <code>MATMUL_LEAF_SIZE</code>.
     *
     * @param A The first matrix.
     * @param B The second matrix.
     * @return The multiplied matrix.
     * @throws LengthException If the matrix sizes are not suitable for multiplication.
     */
    public static double[][] matmul(double[][] A, double[][] B) {
        return matmul(A, B, MATMUL_LEAF_SIZE);
    }

    /**
     * Matrix multiply two real-numbered matrices.
     *
     * @param A        The first matrix.
     * @param B        The second matrix.
     * @param leafSize Size of the matrix before switching to standard IJK multiplication instead of
     *                 Strassen multiplication.
     * @return The multiplied matrix.
     * @throws LengthException If the matrix sizes are not suitable for multiplication.
     */
    public static double[][] matmul(double[][] A, double[][] B, int leafSize) {
        // Check if the matrices can be multiplied
        if (A[0].length != B.length) {
            throw new LengthException("Matrix sizes not suitable for multiplication");
        }

        // Otherwise, perform matrix multiplication
        int numARows = A.length;
        int numCommon = A[0].length;
        int numBCols = B[0].length;

        if ((numARows <= leafSize) && (numCommon <= leafSize) && (numBCols <= leafSize)) {
            return matmulIJK(A, B);
        } else if ((numARows == 1) || (numCommon == 1) || (numBCols == 1)) {
            return matmulIJK(A, B);
        } else {
            return matmulStrassen(A, B, leafSize);
        }
    }

    /**
     * Matrix multiply two complex-numbered matrices.<br>
     * Uses default leaf size of <code>MATMUL_LEAF_SIZE</code>.
     *
     * @param P The first matrix.
     * @param Q The second matrix.
     * @return The multiplied matrix.
     * @throws LengthException If the matrix sizes are not suitable for multiplication.
     */
    public static Complex[][] matmul(Complex[][] P, Complex[][] Q) {
        return matmul(P, Q, MATMUL_LEAF_SIZE);
    }

    /**
     * Matrix multiply two complex-numbered matrices.
     *
     * @param P        The first matrix.
     * @param Q        The second matrix.
     * @param leafSize Size of the matrix before switching to standard IJK multiplication instead of
     *                 Strassen multiplication.
     * @return The multiplied matrix.
     * @throws LengthException If the matrix sizes are not suitable for multiplication.
     */
    public static Complex[][] matmul(Complex[][] P, Complex[][] Q, int leafSize) {
        // Check if the matrices can be multiplied
        if (P[0].length != Q.length) {
            throw new LengthException("Matrix sizes not suitable for multiplication");
        }

        // Get the relevant lengths
        int numPRows = P.length;
        int numCommon = P[0].length;
        int numQCols = Q[0].length;

        // Split the complex matrices into 4 real-valued matrices
        double[][] A = new double[numPRows][numCommon];
        double[][] B = new double[numPRows][numCommon];
        double[][] C = new double[numCommon][numQCols];
        double[][] D = new double[numCommon][numQCols];

        for (int i = 0; i < numPRows; i++) {
            for (int j = 0; j < numCommon; j++) {
                A[i][j] = P[i][j].re;
                B[i][j] = P[i][j].im;
            }
        }

        for (int i = 0; i < numCommon; i++) {
            for (int j = 0; j < numQCols; j++) {
                C[i][j] = Q[i][j].re;
                D[i][j] = Q[i][j].im;
            }
        }

        // Perform required matrix multiplication
        double[][] AC = matmul(A, C, leafSize);
        double[][] BD = matmul(B, D, leafSize);
        double[][] AD = matmul(A, D, leafSize);
        double[][] BC = matmul(B, C, leafSize);

        // Form the final matrix
        double[][] realPart = matsub(AC, BD);
        double[][] imaginaryPart = matadd(AD, BC);

        Complex[][] output = new Complex[numPRows][numQCols];
        for (int i = 0; i < numPRows; i++) {
            for (int j = 0; j < numQCols; j++) {
                output[i][j] = new Complex(realPart[i][j], imaginaryPart[i][j]);
            }
        }

        return output;
    }

    // Matrix modification methods

    /**
     * Transpose a 2D array of doubles.
     *
     * @param array Array of doubles to transpose.
     * @return Transposed array.
     */
    public static double[][] transpose(double[][] array) {
        // Get the dimensions of the original array
        int X = array.length;
        int Y = array[0].length;

        // Convert `double` to `Double`
        Double[][] newArray = new Double[X][Y];
        for (int x = 0; x < X; x++) {
            newArray[x] = TypeConversionUtils.toDoubleArray(array[x]);
        }

        // Create the new array
        Double[][] transposed = new Double[Y][X];

        // Run the transposition process
        transpositionProcess(X, Y, newArray, transposed);

        // Convert `Double` to `double`
        double[][] transposedNew = new double[Y][X];
        for (int y = 0; y < Y; y++) {
            transposedNew[y] = TypeConversionUtils.toDoubleArray(transposed[y]);
        }

        // Return the transposed array
        return transposedNew;
    }

    /**
     * Transpose a 2D array of booleans
     *
     * @param array Array of booleans to transpose.
     * @return Transposed array.
     */
    public static boolean[][] transpose(boolean[][] array) {
        // Get the dimensions of the original array
        int X = array.length;
        int Y = array[0].length;

        // Convert `boolean` to `Boolean`
        Boolean[][] newArray = new Boolean[X][Y];
        for (int x = 0; x < X; x++) {
            newArray[x] = TypeConversionUtils.toBooleanArray(array[x]);
        }

        // Create the new array
        Boolean[][] transposed = new Boolean[Y][X];

        // Run the transposition process
        transpositionProcess(X, Y, newArray, transposed);

        // Convert `Boolean` to `boolean`
        boolean[][] transposedNew = new boolean[Y][X];
        for (int y = 0; y < Y; y++) {
            transposedNew[y] = TypeConversionUtils.toBooleanArray(transposed[y]);
        }

        // Return the transposed array
        return transposedNew;
    }

    /**
     * Transpose a 2D <code>Complex</code> array.
     *
     * @param array Array of <code>Complex</code> objects to transpose.
     * @return Transposed array.
     */
    public static Complex[][] transpose(Complex[][] array) {
        // Get the dimensions of the original array
        int x = array.length;
        int y = array[0].length;

        // Create the new array
        Complex[][] transposed = new Complex[y][x];

        // Run the transposition process
        transpositionProcess(x, y, array, transposed);

        // Return the transposed array
        return transposed;
    }

    // Private methods

    /**
     * IJK matrix multiplication.<br>
     * Assumes that matrices <code>A</code> and <code>B</code> can be multiplied.
     *
     * @param A First matrix.
     * @param B Second matrix.
     * @return Product of the matrices.
     */
    private static double[][] matmulIJK(double[][] A, double[][] B) {
        int numRowsA = A.length;
        int numCommon = A[0].length;
        int numColsB = B[0].length;

        double[][] C = new double[numRowsA][numColsB];

        for (int i = 0; i < numRowsA; i++) {
            for (int j = 0; j < numColsB; j++) {
                double currElem = 0;
                for (int k = 0; k < numCommon; k++) {
                    currElem += A[i][k] * B[k][j];
                }
                C[i][j] = currElem;
            }
        }

        return C;
    }

    /**
     * Strassen matrix multiplication.<br>
     * Assumes that matrices <code>A</code> and <code>B</code> can be multiplied.
     *
     * @param A        First matrix.
     * @param B        Second matrix.
     * @param leafSize Size of the matrix before switching to standard IJK multiplication instead of
     *                 Strassen multiplication.
     * @return Product of the matrices.
     * @see <a href="https://en.wikipedia.org/wiki/Strassen_algorithm">This Wikipedia article</a>
     * on Strassen algorithm for matrix multiplication.
     */
    private static double[][] matmulStrassen(double[][] A, double[][] B, int leafSize) {
        // Lengths
        int numRowsA = A.length;
        int numCommon = A[0].length;
        int numColsB = B[0].length;

        if ((numRowsA <= leafSize) && (numCommon <= leafSize) && (numColsB <= leafSize)) {
            return matmulIJK(A, B);
        } else if ((numRowsA == 1) || (numCommon == 1) || (numColsB == 1)) {
            return matmulIJK(A, B);
        } else {
            // Compute new lengths
            int numRowsANew = numRowsA % 2 == 0 ? numRowsA : numRowsA + 1;
            int numCommonNew = numCommon % 2 == 0 ? numCommon : numCommon + 1;
            int numColsBNew = numColsB % 2 == 0 ? numColsB : numColsB + 1;

            // Pad rows until even
            double[][] Anew = new double[numRowsANew][numCommonNew];
            double[][] Bnew = new double[numCommonNew][numColsBNew];

            for (int i = 0; i < numRowsA; i++) {
                System.arraycopy(A[i], 0, Anew[i], 0, numCommon);
            }

            for (int i = 0; i < numCommon; i++) {
                System.arraycopy(B[i], 0, Bnew[i], 0, numColsB);
            }

            // Define new matrices
            double[][] A11 = new double[numRowsANew / 2][numCommonNew / 2];
            double[][] A12 = new double[numRowsANew / 2][numCommonNew / 2];
            double[][] A21 = new double[numRowsANew / 2][numCommonNew / 2];
            double[][] A22 = new double[numRowsANew / 2][numCommonNew / 2];

            double[][] B11 = new double[numCommonNew / 2][numColsBNew / 2];
            double[][] B12 = new double[numCommonNew / 2][numColsBNew / 2];
            double[][] B21 = new double[numCommonNew / 2][numColsBNew / 2];
            double[][] B22 = new double[numCommonNew / 2][numColsBNew / 2];

            // Copy elements into the matrices
            splitMatrix(Anew, A11, 0, numRowsANew / 2, 0, numCommonNew / 2);
            splitMatrix(Anew, A12, 0, numRowsANew / 2, numCommonNew / 2, numCommonNew);
            splitMatrix(Anew, A21, numRowsANew / 2, numRowsANew, 0, numCommonNew / 2);
            splitMatrix(Anew, A22, numRowsANew / 2, numRowsANew, numCommonNew / 2, numCommonNew);

            splitMatrix(Bnew, B11, 0, numCommonNew / 2, 0, numColsBNew / 2);
            splitMatrix(Bnew, B12, 0, numCommonNew / 2, numColsBNew / 2, numColsBNew);
            splitMatrix(Bnew, B21, numCommonNew / 2, numCommonNew, 0, numColsBNew / 2);
            splitMatrix(Bnew, B22, numCommonNew / 2, numCommonNew, numColsBNew / 2, numColsBNew);

            // Apply Strassen-Winograd Formulae
            double[][] mulA11B11 = matmulStrassen(A11, B11, leafSize);
            double[][] subA21A11 = matsub(A21, A11);
            double[][] subB12B22 = matsub(B12, B22);
            double[][] subB12B11 = matsub(B12, B11);

            double[][] U = matmulStrassen(matsub(A21, A11), matsub(B12, B22), leafSize);
            double[][] V = matmulStrassen(matadd(A21, A22), subB12B11, leafSize);
            double[][] W = matadd(
                    mulA11B11, matmulStrassen(matsub(matadd(A21, A22), A11), matsub(B22, subB12B11), leafSize)
            );
            double[][] addVW = matadd(V, W);

            double[][] C11 = matadd(mulA11B11, matmulStrassen(A12, B21, leafSize));
            double[][] C12 = matadd(addVW, matmulStrassen(matsub(matsub(A12, subA21A11), A22), B22, leafSize));
            double[][] C21 = matadd(matadd(U, W), matmulStrassen(A22, matadd(matsub(B21, B11), subB12B22), leafSize));
            double[][] C22 = matadd(U, addVW);

            // Join into one matrix
            double[][] CNew = new double[numRowsANew][numColsBNew];
            joinMatrices(CNew, C11, 0, 0);
            joinMatrices(CNew, C12, 0, numColsBNew / 2);
            joinMatrices(CNew, C21, numRowsANew / 2, 0);
            joinMatrices(CNew, C22, numRowsANew / 2, numColsBNew / 2);

            // Remove unneeded elements
            double[][] C = new double[numRowsA][numColsB];
            for (int i = 0; i < numRowsA; i++) {
                System.arraycopy(CNew[i], 0, C[i], 0, numColsB);
            }

            return C;
        }
    }

    /**
     * Splits a matrix and places the result in <code>newMatrix</code>.
     *
     * @param matrix    Original matrix to split.
     * @param newMatrix New matrix that contains the split elements.
     * @param rowStart  Starting row index to start the splitting (inclusive).
     * @param rowEnd    Ending row index to end the splitting (exclusive).
     * @param colStart  Starting column index to start the splitting (inclusive).
     * @param colEnd    Ending column index to end the splitting (exclusive).
     */
    private static void splitMatrix(
            double[][] matrix, double[][] newMatrix, int rowStart, int rowEnd, int colStart, int colEnd
    ) {
        for (int i = rowStart; i < rowEnd; i++) {
            System.arraycopy(matrix[i], colStart, newMatrix[i - rowStart], 0, colEnd - colStart);
        }
    }

    /**
     * Joins the <code>childMatrix</code> into the <code>parentMatrix</code> at the position
     * <code>(rowStart, colStart)</code>.
     *
     * @param parentMatrix Parent matrix.
     * @param childMatrix  Matrix to join into the parent.
     * @param rowStart     Starting point of the join for the rows.
     * @param colStart     Starting point of the join for the columns.
     */
    private static void joinMatrices(
            double[][] parentMatrix, double[][] childMatrix, int rowStart, int colStart
    ) {
        for (int i1 = 0, i2 = rowStart; i1 < childMatrix.length; i1++, i2++) {
            for (int j1 = 0, j2 = colStart; j1 < childMatrix[0].length; j1++, j2++) {
                parentMatrix[i2][j2] = childMatrix[i1][j1];
            }
        }
    }

    /**
     * Helper method that transposes the original array <code>originalArray</code> and places the
     * result into <code>finalArray</code>. <b>This is an in-place method</b>.
     *
     * @param X             Number of subarrays in <code>originalArray</code>.
     * @param Y             Length of each subarray in <code>originalArray</code>.
     * @param originalArray The original, un-transposed, array.
     * @param finalArray    Array to contain the transposed array.
     * @param <T>           Type of the array.
     */
    private static <T> void transpositionProcess(int X, int Y, T[][] originalArray, T[][] finalArray) {
        for (int y = 0; y < Y; y++) {
            for (int x = 0; x < X; x++) {
                finalArray[y][x] = originalArray[x][y];
            }
        }
    }
}
