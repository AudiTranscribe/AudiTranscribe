/*
 * ArrayUtils.java
 * Description: Array utilities.
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
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.misc.Complex;

/**
 * Array utilities.
 */
public final class ArrayUtils {
    // Constants
    public static final int MATMUL_LEAF_SIZE = 2048;  // Leaf size for matrix multiplication

    private ArrayUtils() {
        // Private constructor to signal this is a utility class
    }

    // Searching methods

    /**
     * Method that locates local maxima in an array.<br>
     * An element <code>array[i]</code> is considered a local maximum if both conditions are met:
     * <ul>
     *     <li><code>array[i] > array[i-1]</code></li>
     *     <li><code>array[i] >= array[i+1]</code></li>
     * </ul>
     * Note the first element (<code>array[0]</code>) will <b>never</b> be considered as a local
     * maximum.
     *
     * @param array Array of values.
     * @return A boolean array. If the index has <code>true</code> at the spot, then the
     * corresponding element is a local maximum.
     */
    public static boolean[] findLocalMaxima(double[] array) {
        boolean[] isLocalMaximum = new boolean[array.length];
        isLocalMaximum[0] = false;  // Edge case: first element never a local maximum

        for (int i = 1; i < array.length; i++) {
            if (i == array.length - 1) {  // Edge case: last element only needs to check the one before
                isLocalMaximum[array.length - 1] = array[i] > array[i - 1];
            } else {
                isLocalMaximum[i] = ((array[i] > array[i - 1]) && (array[i] >= array[i + 1]));
            }
        }

        return isLocalMaximum;
    }

    /**
     * Find the index where an element should be inserted to maintain order in a sorted array.<br>
     * <br>
     * Finds the index into a <b>sorted</b> array <code>array</code> such that, if the
     * <code>value</code> was inserted <b>before</b> the index, the order of elements in
     * <code>array</code> would be preserved.
     *
     * @param array The array of <b>sorted</b> elements.<br>
     *              <b>No checks are in place to check that this array is sorted</b>.
     * @param value The value to insert into <code>array</code>.
     * @return Index to insert <code>value</code> into <code>array</code> to maintain order.
     * @implNote Taken from <a href="https://tinyurl.com/NumSharpSearchSorted">NumSharp's
     * Implementation</a> of the <code>searchSorted</code> method in C#. See also
     * <a href="https://tinyurl.com/2p9499dy">NumPy's Documentation</a> on how the function works.
     */
    public static int searchSorted(double[] array, double value) {
        // Get the length of the array
        int n = array.length;

        // Perform 'trivial' checks
        if (value <= array[0]) return 0;  // If the value is smaller than or equals minimum, return smallest index
        if (value > array[n - 1]) return n;  // If the value is larger than maximum, return next index after largest

        // Perform iterative binary search
        int leftPtr = 0;
        int rightPtr = n - 1;
        int middlePtr;

        while (leftPtr < rightPtr) {
            middlePtr = (leftPtr + rightPtr) / 2;
            double middle = array[middlePtr];

            // Compare 'middle' value with the target value
            if (middle < value) {
                leftPtr = middlePtr + 1;
            } else if (middle == value) {
                return middlePtr;
            } else {
                rightPtr = middlePtr;
            }
        }

        // Return the left pointer
        return leftPtr;
    }

    // Array generation methods

    /**
     * Slice a data array into (overlapping) horizontal frames.<br>
     * This means an array <code>[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]</code> with
     * <code>frameLength = 3</code> and <code>hopLength = 2</code> will be framed into
     * <code>[[1, 2, 3], [3, 4, 5], [5, 6, 7], [7, 8, 9], [9, 10, 11]]</code>.
     *
     * @param array       Array to frame.
     * @param frameLength Length of each frame.
     * @param hopLength   Number of steps to advance between frames.
     * @return Horizontally framed view of <code>array</code>.
     */
    public static double[][] frameHorizontal(double[] array, int frameLength, int hopLength) {
        return frameHelper(array, frameLength, hopLength, false);
    }

    /**
     * Slice a data array into (overlapping) vertical frames.<br>
     * This means an array <code>[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]</code> with
     * <code>frameLength = 3</code> and <code>hopLength = 2</code> will be framed into
     * <code>[[1, 3, 5, 7, 9], [2, 4, 6, 8, 10], [3, 5, 7, 9, 11]]</code>.
     *
     * @param array       Array to frame.
     * @param frameLength Length of each frame.
     * @param hopLength   Number of steps to advance between frames.
     * @return Vertically framed view of <code>array</code>.
     */
    public static double[][] frameVertical(double[] array, int frameLength, int hopLength) {
        return frameHelper(array, frameLength, hopLength, true);
    }

    /**
     * Return evenly spaced numbers over a specified interval.<br>
     * Returns <code>numElem</code> evenly spaced samples from the interval
     * <code>[start, end]</code> (including endpoints).
     *
     * @param start   Starting value.
     * @param end     Ending value.
     * @param numElem Number of samples.
     * @return Array of samples.
     */
    public static double[] linspace(double start, double end, int numElem) {
        // Handle weird cases
        if (numElem == 0) return new double[0];
        if (numElem == 1) return new double[]{start};

        // Handle standard cases
        double scale = (end - start) / (numElem - 1.);

        double[] out = new double[numElem];
        out[0] = start;
        out[numElem - 1] = end;
        for (int i = 1; i < numElem - 1; i++) {
            out[i] = i * scale + start;
        }
        return out;
    }

    // Array modification methods

    /**
     * Normalizes the elements in the given array such that the L<sup>p</sup> norm is 1.
     *
     * @param array The array to normalize.
     * @param norm  The norm parameter for the L<sup>p</sup> normalization. There are 5 cases to
     *              consider for the value of <code>norm</code>.
     *              <ul>
     *                  <li>
     *                      <code>norm = Double.NEGATIVE_INFINITY</code>: The L<sup>p</sup> norm is
     *                      considered to be the <b>minimum</b> absolute value.
     *                  </li>
     *                  <li>
     *                      <code>norm = Double.POSITIVE_INFINITY</code>: The L<sup>p</sup> norm is
     *                      considered to be the <b>maximum</b> absolute value.
     *                  </li>
     *                  <li>
     *                      <code>norm = 0</code>: The L<sup>p</sup> norm is considered to be the
     *                      sum of all magnitudes.
     *                  </li>
     *                  <li>
     *                      <code>norm > 0</code>: The <code>p</code> value is equal to
     *                      <code>norm</code>, and L<sup>p</sup> norm will be calculated normally.
     *                  </li>
     *                  <li>
     *                      <code>norm < 0</code>: No normalization will be performed.
     *                  </li>
     *              </ul>
     * @return The normalized complex array such that the L<sup>p</sup> norm of the array is 1.
     * @see <a href="https://bit.ly/3LVePPv">L<sup>p</sup>-Norm</a> on Wikipedia, and
     * <a href="https://bit.ly/3GwhYUJ">Librosa's Implementation</a> of this method.
     */
    public static Complex[] lpNormalize(Complex[] array, double norm) {
        // Get the number of elements in the array
        int numElem = array.length;

        // Set threshold to be the smallest (in absolute terms) non-zero number supported
        double threshold = Double.MIN_VALUE;

        // Get the magnitudes of the data in the array
        double[] magnitudes = new double[numElem];

        for (int i = 0; i < numElem; i++) {
            magnitudes[i] = array[i].abs();
        }

        // Determine the `lpNorm` value
        double lpNorm = 0;

        if (norm == Double.NEGATIVE_INFINITY) {
            // `p` value is the minimum absolute value of the values in the array
            double minAbsVal = Double.MAX_VALUE;
            for (double absVal : magnitudes) {
                if (absVal < minAbsVal) {
                    minAbsVal = absVal;
                }
            }
            lpNorm = minAbsVal;
        } else if (norm == Double.POSITIVE_INFINITY) {
            // `p` value is the maximum absolute value of the values in the array
            double maxAbsVal = Double.MIN_VALUE;
            for (double absVal : magnitudes) {
                if (absVal > maxAbsVal) {
                    maxAbsVal = absVal;
                }
            }
            lpNorm = maxAbsVal;
        } else if (norm == 0) {
            // The `lpNorm` is the number of positive values in the array
            for (double mag : magnitudes) {
                if (mag > 0) lpNorm++;
            }
        } else if (norm > 0) {
            // Use the LP norm formula
            for (double mag : magnitudes) {
                lpNorm += Math.pow(mag, norm);
            }
            lpNorm = Math.pow(lpNorm, 1. / norm);

        } else {  // `norm` is negative
            // Do not perform normalization
            return array;
        }

        // Ensure that the Lp-norm is at least the threshold
        if (lpNorm < threshold) {
            lpNorm = threshold;
        }

        // Normalize the array
        Complex[] normalizedArray = new Complex[numElem];

        for (int i = 0; i < numElem; i++) {
            normalizedArray[i] = array[i].divides(lpNorm);
        }

        // Return the Lp-normalised array
        return normalizedArray;
    }


    /**
     * Normalizes the elements in the given array such that the L<sup>p</sup> norm is 1.
     *
     * @param array The array to normalize.
     * @param norm  The norm parameter for the L<sup>p</sup> normalization. There are 5 cases to
     *              consider for the value of <code>norm</code>.
     *              <ul>
     *                  <li>
     *                      <code>norm = Double.NEGATIVE_INFINITY</code>: The L<sup>p</sup> norm is
     *                      considered to be the <b>minimum</b> absolute value.
     *                  </li>
     *                  <li>
     *                      <code>norm = Double.POSITIVE_INFINITY</code>: The L<sup>p</sup> norm is
     *                      considered to be the <b>maximum</b> absolute value.
     *                  </li>
     *                  <li>
     *                      <code>norm = 0</code>: The L<sup>p</sup> norm is considered to be the
     *                      sum of all magnitudes.
     *                  </li>
     *                  <li>
     *                      <code>norm > 0</code>: The <code>p</code> value is equal to
     *                      <code>norm</code>, and L<sup>p</sup> norm will be calculated normally.
     *                  </li>
     *                  <li>
     *                      <code>norm < 0</code>: No normalization will be performed.
     *                  </li>
     *              </ul>
     * @return The normalized complex array such that the L<sup>p</sup> norm of the array is 1.
     * @see <a href="https://bit.ly/3LVePPv">L<sup>p</sup>-Norm</a> on Wikipedia, and
     * <a href="https://bit.ly/3GwhYUJ">Librosa's Implementation</a> of this method.
     */
    public static double[] lpNormalize(double[] array, double norm) {
        // Convert all `double` values into `Complex` values
        Complex[] complexArray = new Complex[array.length];
        for (int i = 0; i < array.length; i++) {
            complexArray[i] = new Complex(array[i]);
        }

        // Normalize the array
        complexArray = lpNormalize(complexArray, norm);

        // Convert all `Complex` values back into `double` values
        double[] normalizedArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            normalizedArray[i] = complexArray[i].re;
        }

        // Return the normalized array
        return normalizedArray;
    }

    /**
     * Pad an array <code>array</code> to length <code>size</code> by centering the pre-existing
     * elements in <code>array</code>.
     *
     * @param array The array to pad.
     * @param size  The size to make the array.
     * @return Padded array where the length is now <code>size</code>.
     * @throws ValueException If <code>size</code> is smaller than the input array's length.
     */
    public static double[] padCenter(double[] array, int size) {
        // Get length of the array
        int n = array.length;

        // If `n` is `size` just return the array
        if (n == size) {
            return array;
        }

        // Assert that the length of the data at least the desired size
        if (size < n) {
            throw new ValueException("Target size (" + size + ") must be at least input size (" + n + ")");
        }

        // Calculate left padding
        int lpad = (size - n) / 2;

        // Fill in the output array
        double[] output = new double[size];
        System.arraycopy(array, 0, output, lpad, n);

        return output;
    }

    /**
     * Pad an array <code>array</code> to length <code>size</code> by centering the pre-existing
     * elements in <code>array</code>.
     *
     * @param array The array to pad.
     * @param size  The size to make the array.
     * @return Padded array where the length is now <code>size</code>.
     * @throws ValueException If <code>size</code> is smaller than the input array's length.
     */
    public static Complex[] padCenter(Complex[] array, int size) {
        // Get length of the array
        int n = array.length;

        // If `n` is `size` just return the array
        if (n == size) {
            return array;
        }

        // Assert that the length of the data at least the desired size
        if (size < n) {
            throw new ValueException("Target size (" + size + ") must be at least input size (" + n + ")");
        }

        // Calculate left padding
        int lpad = (size - n) / 2;

        // Fill in the output array
        Complex[] output = new Complex[size];
        for (int i = 0; i < size; i++) {
            output[i] = Complex.ZERO;
        }
        System.arraycopy(array, 0, output, lpad, n);

        return output;
    }

    // Matrix methods

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

    // Private methods

    /**
     * Helper method that slices a data array into (overlapping) frames.<br>
     * For example, an array <code>[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]</code> with
     * <code>frameLength = 3</code> and <code>hopLength = 2</code> will be framed into
     * <ul>
     *     <li>
     *         <code>[[1, 3, 5, 7, 9], [2, 4, 6, 8, 10], [3, 5, 7, 9, 11]]</code> if
     *         <code>verticalFraming</code> is true; and
     *     </li>
     *     <li>
     *         <code>[[1, 2, 3], [3, 4, 5], [5, 6, 7], [7, 8, 9], [9, 10, 11]]</code> if not (that
     *         is, framed horizontally).
     *     </li>
     * </ul>
     *
     * @param array           Array to frame.
     * @param frameLength     Length of each frame.
     * @param hopLength       Number of steps to advance between frames.
     * @param verticalFraming If true, will frame vertically. Otherwise, will frame horizontally.
     * @return Framed view of <code>array</code>.
     * @implNote See <a href="https://stackoverflow.com/a/38163917">this StackOverflow answer</a>
     * for implementation details in Python.
     */
    private static double[][] frameHelper(
            double[] array, int frameLength, int hopLength, boolean verticalFraming
    ) {
        // Calculate the length of the framed array
        int finalArrayLength = Math.floorDiv(array.length - frameLength, hopLength) + 1;

        // Create the blank array to store the framed data in
        double[][] framed;
        if (verticalFraming) {
            framed = new double[frameLength][finalArrayLength];
        } else {
            framed = new double[finalArrayLength][frameLength];
        }

        // Fill in the array
        int length = array.length;

        for (int index = 0, endFrameIndex = 0; endFrameIndex < length;
             index++, endFrameIndex += hopLength) {  // Iterate through the `framed` array
            for (int i = 0; i < frameLength; i++) {  // Iterate through the frame
                // Validate the value of `i`
                if (i + endFrameIndex < length && index < finalArrayLength) {
                    if (verticalFraming) {
                        framed[i][index] = array[i + endFrameIndex];
                    } else {
                        framed[index][i] = array[i + endFrameIndex];
                    }
                }
            }
        }

        return framed;
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
}
