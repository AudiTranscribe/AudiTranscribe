/*
 * ArrayUtils.java
 *
 * Created on 2022-02-16
 * Updated on 2022-03-14
 *
 * Description: Array utilities to modify and change arrays.
 */

package site.overwrite.auditranscribe.utils;

import java.security.InvalidParameterException;

/**
 * Array utilities.
 */
public class ArrayUtils {
    /**
     * Normalises the elements in the given array such that the LP norm is 1.
     *
     * @param array The array to normalise.
     * @param p     The p value of the LP norm.
     * @return The normalised complex array.
     * @throws IllegalArgumentException If <code>p</code> is negative.
     * @see <a href="https://en.wikipedia.org/wiki/Lp_space#The_p-norm_in_finite_dimensions">
     * L<sup>p</sup>-Norm</a>
     */
    public static Complex[] lpNormalise(Complex[] array, double p) {
        // Check that `p` is strictly non-negative
        if (p < 0) {
            throw new IllegalArgumentException("Unsupported p value: " + p);
        }

        // Get the number of elements in the array
        int numElem = array.length;

        // Set threshold to be the smallest non-zero number supported
        double threshold = Double.MIN_VALUE;

        // Get the magnitudes of the data in `array`
        double[] magnitudes = new double[numElem];

        for (int i = 0; i < numElem; i++) {
            magnitudes[i] = array[i].abs();
        }

        // Compute the current `lpNorm` of `array`
        double lpNorm = 0;

        if (p == 0) {
            // The `lpNorm` is the sum of all magnitudes
            for (double mag : magnitudes) {
                lpNorm += mag;
            }
        } else {  // We've already checked if `p < 0`, so here we know `p > 0`
            // Use the LP norm formula
            for (double mag : magnitudes) {
                lpNorm += Math.pow(mag, p);
            }
            lpNorm = Math.pow(lpNorm, 1.0 / p);
        }

        // Ensure that the LP Norm is at least the threshold
        if (lpNorm < threshold) {
            lpNorm = threshold;  // Todo: in Python, this is 1. Does this still work if set to `threshold`?
        }

        // Normalise the signal
        Complex[] arrayNormalised = new Complex[numElem];

        for (int i = 0; i < numElem; i++) {
            arrayNormalised[i] = array[i].divides(lpNorm);
        }

        // Return the normalised signal
        return arrayNormalised;
    }

    /**
     * Fix the length an array <code>data</code> to exactly <code>size</code>.
     *
     * @param data The array to fix the length of.
     * @param size The size to make the array.
     * @return <code>data</code> array where the length is now <code>size</code>.
     * @throws IllegalArgumentException If <code>size</code> is negative.
     */
    public static double[] fixLength(double[] data, int size) {
        // Assert `size` is non-negative
        if (size < 0) {
            throw new IllegalArgumentException("Invalid size " + size);
        }

        // Get length of data
        int n = data.length;

        // If `n` is `size` just return `data`
        if (n == size) {
            return data;
        }

        // Otherwise, fill in the output array
        double[] output = new double[size];
        System.arraycopy(data, 0, output, 0, Math.min(n, size));

        // Return the output data
        return output;
    }

    /**
     * Pad an array <code>data</code> to length <code>size</code> by centering the pre-existing data
     * in <code>data</code>.
     *
     * @param data The array to pad.
     * @param size The size to make the array.
     * @return <code>data</code> array where the length is now <code>size</code>.
     * @throws IllegalArgumentException If <code>size</code> is negative.
     */
    public static double[] padCenter(double[] data, int size) {
        // Assert `size` is non-negative
        if (size < 0) {
            throw new IllegalArgumentException("Invalid size " + size);
        }

        // Get length of data
        int n = data.length;

        // If `n` is `size` just return `data`
        if (n == size) {
            return data;
        }

        // Calculate left padding
        int lpad = (size - n) / 2;

        // Fill in the output data
        double[] output = new double[size];
        System.arraycopy(data, 0, output, lpad, n);

        // Return the output data
        return output;
    }

    /**
     * Pad an array <code>data</code> to length <code>size</code> by centering the pre-existing data
     * in <code>data</code>.
     *
     * @param data The array to pad.
     * @param size The size to make the array.
     * @return <code>data</code> array where the length is now <code>size</code>.
     * @throws IllegalArgumentException If <code>size</code> is negative.
     */
    public static Complex[] padCenter(Complex[] data, int size) {
        // Assert `size` is non-negative
        if (size < 0) {
            throw new IllegalArgumentException("Invalid size " + size);
        }

        // Get length of data
        int n = data.length;

        // If `n` is `size` just return `data`
        if (n == size) {
            return data;
        }

        // Calculate left padding
        int lpad = (size - n) / 2;

        // Fill in the output data
        Complex[] output = new Complex[size];
        for (int i = 0; i < size; i++) {
            output[i] = new Complex(0, 0);
        }

        // Copy the existing data into the array
        System.arraycopy(data, 0, output, lpad, n);

        // Return the output data
        return output;
    }

    /**
     * Find the index where an element should be inserted to maintain order in a sorted array.<br>
     *
     * Find the index into a <b>sorted</b> array <code>array</code> such that, if the
     * <code>value</code> was inserted <b>before</b> the index, the order of elements in
     * <code>array</code> would be preserved.
     *
     * @param array The array of <b>sorted</b> elements. <b>No checks are in place to assert that
     *              this array is sorted</b>.
     * @param value The value to insert into <code>array</code>.
     * @return  Index to insert <code>value</code> into <code>array</code> to maintain order.
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

        // Define left, right, and middle pointers
        int left = 0;
        int right = n - 1;
        int middle;

        // Perform iterative binary search
        while (left < right) {
            // Calculate middle
            middle = (left + right) / 2;

            // Compare 'middle' value with the target value
            if (array[middle] < value) {
                left = middle + 1;
            } else if (array[middle] == value) {
                return middle;
            } else {
                right = middle;
            }
        }

        // Return left pointer
        return left;
    }

    /**
     * Slice a data array into (overlapping) frames.<br>
     * This means an array <code>[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]</code> with
     * <code>frameLength = 3</code> and <code>hopLength = 2</code> will be framed into
     * <code>[[1, 3, 5, 7, 9], [2, 4, 6, 8, 10], [3, 5, 7, 9, 11]]</code> if
     * <code>verticalFraming</code> is true and
     * <code>[[1, 2, 3], [3, 4, 5], [5, 6, 7], [7, 8, 9], [9, 10, 11]]</code> if not (that is,
     * framed horizontally).
     *
     * @param array           Array to frame.
     * @param frameLength     Length of the frame.
     * @param hopLength       Number of steps to advance between frames.
     * @param verticalFraming If true, will frame vertically. Otherwise will frame horizontally.
     * @return Framed view of <code>array</code>.
     * @implNote See <a href="https://stackoverflow.com/a/38163917">this StackOverflow answer</a>
     * for implementation details in Python.
     */
    public static double[][] frame(double[] array, int frameLength, int hopLength, boolean verticalFraming) {
        // Calculate the length of the framed array
        int finalArrayLength = (int) Math.floor((double) (array.length - frameLength) / hopLength) + 1;

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

        // Return the `framed` array
        return framed;
    }

    /**
     * Transpose a 2D <code>Complex</code> array.
     * @param array Array of <code>Complex</code> objects to transpose.
     * @return  Transposed array.
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
     * Transpose a 2D array of doubles.
     * @param array Array of doubles to transpose.
     * @return  Transposed array.
     */
    public static double[][] transpose(double[][] array) {
        // Get the dimensions of the original array
        int X = array.length;
        int Y = array[0].length;

        // Convert `double` to `Double`
        Double[][] newArray = new Double[X][Y];
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                newArray[x][y] = array[x][y];
            }
        }

        // Create the new array
        Double[][] transposed = new Double[Y][X];

        // Run the transposition process
        transpositionProcess(X, Y, newArray, transposed);

        // Convert `Double` to `double`
        double[][] transposedNew = new double[Y][X];
        for (int y = 0; y < Y; y++) {
            for (int x = 0; x < X; x++) {
                transposedNew[y][x] = transposed[y][x];
            }
        }

        // Return the transposed array
        return transposedNew;
    }

    /**
     * Matrix multiply two complex numbered matrices.
     * Todo: see if can make this more efficient.
     *
     * @param A The first matrix.
     * @param B The second matrix.
     * @return The multiplied matrix.
     * @throws InvalidParameterException If the matrix sizes are not suitable for multiplication.
     * @see <a href="https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm">This article</a>
     * on the naiive implementation on the Matrix Multiplication algorithm.
     */
    public static Complex[][] matmul(Complex[][] A, Complex[][] B) {
        // Check if the matrices can be multiplied
        if (A[0].length != B.length) {
            throw new InvalidParameterException("Matrix sizes not suitable for multiplication");
        }

        // Otherwise, perform matrix multiplication
        int n = A.length;
        int m = A[0].length;
        int p = B[0].length;

        Complex[][] output = new Complex[A.length][B[0].length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                // Calculate the value of the current element
                Complex currElem = new Complex(0, 0);
                for (int k = 0; k < m; k++) {
                    currElem = currElem.plus(Complex.times(A[i][k], B[k][j]));
                }

                // Set the current element to the output matrix
                output[i][j] = currElem;
            }
        }

        // Return the output
        return output;
    }

    // Private methods
    private static <T> void transpositionProcess(int X, int Y, T[][] originalArray, T[][] finalArray) {
        for (int y = 0; y < Y; y++) {
            for (int x = 0; x < X; x++) {
                finalArray[y][x] = originalArray[x][y];
            }
        }
    }

//    /**
//     * Slice a data array into (overlapping) frames.
//     *
//     * @param array       Array to frame.
//     * @param frameLength Length of the frame.
//     * @param hopLength   Number of steps to advance between frames.
//     * @return Framed view of <code>array</code>.
//     * @implNote See <a href="https://stackoverflow.com/a/38163917">this StackOverflow answer</a>
//     * for implementation details in Python.
//     */
//    public static double[][] frameOld(double[] array, int frameLength, int hopLength) {
//        // Calculate the length of the framed array
//        int finalArrayLength = (int) Math.ceil((double) array.length / hopLength);
//
//        // Create the blank array to store the framed data in
//        double[][] framed = new double[finalArrayLength][frameLength];
//
//        // Fill in the array
//        int length = array.length;
//
//        for (int index = 0, endFrameIndex = 0; endFrameIndex < length; index++, endFrameIndex += hopLength) {  // Iterate through the `framed` array
//            for (int i = 0; i < frameLength; i++) {  // Iterate through the frame
//                // Validate the value of `i`
//                if (i + endFrameIndex < length) {
//                    framed[index][i] = array[i + endFrameIndex];
//                }
//            }
//        }
//
//        // Return the `framed` array
//        return framed;
//    }

//
//    /**
//     * Slice a data array into (overlapping) frames.
//     *
//     * @param array       Array to frame.
//     * @param frameLength Length of the frame.
//     * @param hopLength   Number of steps to advance between frames.
//     * @return Framed view of <code>array</code>.
//     * @implNote See <a href="https://stackoverflow.com/a/38163917">this StackOverflow answer</a>
//     * for implementation details in Python.
//     *
//     * Todo: is this obsolete now?
//     */
//    public static float[][] frame(float[] array, int frameLength, int hopLength) {
//        // Calculate the length of the framed array
//        int finalArrayLength = (int) Math.ceil((double) array.length / hopLength);
//
//        // Create the blank array to store the framed data in
//        float[][] framed = new float[finalArrayLength][frameLength];
//
//        // Fill in the array
//        int length = array.length;
//
//        for (int index = 0, endFrameIndex = 0; endFrameIndex < length; index++, endFrameIndex += hopLength) {  // Iterate through the `framed` array
//            for (int i = 0; i < frameLength; i++) {  // Iterate through the frame
//                // Validate the value of `i`
//                if (i + endFrameIndex < length) {
//                    framed[index][i] = array[i + endFrameIndex];
//                }
//            }
//        }
//
//        // Return the `framed` array
//        return framed;
//    }
}
