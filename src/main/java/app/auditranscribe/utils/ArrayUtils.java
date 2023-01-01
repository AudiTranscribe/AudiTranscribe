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

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.misc.Complex;

/**
 * Array utilities.
 */
public final class ArrayUtils {
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
     * Pad an array <code>array</code> to length <code>size</code> by centering the pre-existing
     * elements in <code>array</code>.
     *
     * @param array The array to pad.
     * @param size  The size to make the array.
     * @return Padded array where the length is now <code>size</code>.
     * @throws ValueException If <code>size</code> is negative.
     */
    public static double[] padCenter(double[] array, int size) {
        // Get length of the array
        int n = array.length;

        // Assert that the length of the data at least the desired size
        if (size < n) {
            throw new ValueException("Target size (" + size + ") must be at least input size (" + n + ")");
        }

        // If `n` is `size` just return the array
        if (n == size) {
            return array;
        }

        // Calculate left padding
        int lpad = (size - n) / 2;

        // Fill in the output array
        double[] output = new double[size];
        System.arraycopy(array, 0, output, lpad, n);

        // Return the output array
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
}
