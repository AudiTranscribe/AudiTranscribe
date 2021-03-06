/*
 * ArrayUtils.java
 *
 * Created on 2022-02-16
 * Updated on 2022-06-28
 *
 * Description: Array utility methods to modify, change, and search within arrays.
 */

package site.overwrite.auditranscribe.utils;

import site.overwrite.auditranscribe.exceptions.generic.LengthException;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.misc.Complex;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Array utility methods to modify, change, and search within arrays.
 */
public final class ArrayUtils {
    private ArrayUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that finds the index of an element in an array using linear search.<br>
     * If there are multiple copies of the same element, this will return the <b>earliest occurring
     * index</b> of that element.
     *
     * @param array Array of items.
     * @param elem  Element to get the index of.
     * @param <T>   Type of the array.
     * @return Integer representing the index of the element in the array.
     * @throws NoSuchElementException If the element <code>elem</code> does not appear in the
     *                                array <code>array</code>.
     */
    public static <T> int findIndex(T[] array, T elem) {
        // Get the length of the array
        int n = array.length;

        // Try and find the index
        for (int i = 0; i < n; i++) {
            if (array[i].equals(elem)) {
                return i;
            }
        }

        // If reached here then the element does not exist
        throw new NoSuchElementException("Element " + elem + " does not exist in the array.");
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
            lpNorm = Math.pow(lpNorm, 1.0 / norm);

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
     * @param norm  The norm parameter for the L<sup>p</sup> normalization.
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
            normalizedArray[i] = complexArray[i].re();
        }

        // Return the normalized array
        return normalizedArray;
    }

    /**
     * Fix the length an array <code>array</code> to exactly <code>size</code>.
     *
     * @param array The array to fix the length of.
     * @param size  The size to make the array.
     * @return <code>array</code> array where the length is now <code>size</code>.
     * @throws ValueException If <code>size</code> is negative.
     */
    public static double[] fixLength(double[] array, int size) {
        // Verify that `size` is positive
        if (size <= 0) {
            throw new ValueException("Invalid size " + size);
        }

        // Get length of the array
        int n = array.length;

        // If `n` is `size` just return the array
        if (n == size) {
            return array;
        }

        // Otherwise, fill in the output array
        double[] output = new double[size];
        System.arraycopy(array, 0, output, 0, Math.min(n, size));

        // Return the output array
        return output;
    }

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

    /**
     * Pad an array <code>array</code> to length <code>size</code> by centering the pre-existing
     * elements in <code>array</code>.
     *
     * @param array The array to pad.
     * @param size  The size to make the array.
     * @return Padded array where the length is now <code>size</code>.
     * @throws ValueException If <code>size</code> is smaller than the input size.
     */
    public static Complex[] padCenter(Complex[] array, int size) {
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
        Complex[] output = new Complex[size];
        for (int i = 0; i < size; i++) {
            output[i] = Complex.ZERO;
        }

        // Copy the existing array into the array
        System.arraycopy(array, 0, output, lpad, n);

        // Return the output array
        return output;
    }

    /**
     * Pad an array <code>array</code> to length <code>size</code> by centering the pre-existing
     * elements in <code>array</code> and using mode "reflect" to handle padding.
     *
     * @param array The array to pad.
     * @param size  The size to make the array.
     * @return Padded array where the length is now <code>size</code>.
     * @throws ValueException If <code>size</code> is negative.
     * @see <a href="https://bit.ly/3z73f0U">SciPy's intended implementation</a> of the reflection
     * mode.
     */
    public static double[] padCenterReflect(double[] array, int size) {
        // Verify that `size` is positive
        if (size <= 0) {
            throw new ValueException("Invalid size " + size);
        }

        // Get length of the array
        int n = array.length;

        // If `n` is `size` just return the array
        if (n == size) {
            return array;
        }

        // Calculate left and right padding
        int lpad = (size - n) / 2;
        int rpad = size - n - lpad;

        // Fill the output array
        double[] output = new double[size];
        System.arraycopy(array, 0, output, lpad, n);  // Copy center elements

        int reflectedElemIndex = 0;
        boolean doubleCount = true;  // Whether to include the current element twice
        int changeInVal = n == 1 ? 0 : 1;  // Don't add anything if there is only 1 element
        for (int i = 0; i < lpad; i++) {  // Left padding
            // Handle reflected element index calculation
            if (reflectedElemIndex + changeInVal >= n) {
                changeInVal = -1;
                doubleCount = true;
            }

            if (reflectedElemIndex + changeInVal < 0) {
                changeInVal = 1;
                doubleCount = true;
            }

            if (!doubleCount) {
                reflectedElemIndex += changeInVal;
            } else {
                doubleCount = false;
            }

            // Get the position to place this element
            int placementIndex = lpad - i - 1;  // We start from the back of the section to be padded

            // Place the element
            output[placementIndex] = array[reflectedElemIndex];
        }

        reflectedElemIndex = n - 1;  // Start from the end
        doubleCount = true;
        changeInVal = n == 1 ? 0 : -1;  // Don't add anything if there is only 1 element
        for (int i = 0; i < rpad; i++) {  // Right padding
            // Handle reflected element index calculation
            if (reflectedElemIndex + changeInVal >= n) {
                changeInVal = -1;
                doubleCount = true;
            }

            if (reflectedElemIndex + changeInVal < 0) {
                changeInVal = 1;
                doubleCount = true;
            }

            if (!doubleCount) {
                reflectedElemIndex += changeInVal;
            } else {
                doubleCount = false;
            }

            // Get the position to place this element
            int placementIndex = size - rpad + i;

            // Place the element
            output[placementIndex] = array[reflectedElemIndex];
        }

        // Return the output array
        return output;
    }

    /**
     * Take certain elements of an array and return them in a new array.
     *
     * @param array   The array to take elements from.
     * @param indices The indices to take.
     * @return The new array containing the selected elements.
     */
    public static double[] takeElem(double[] array, int[] indices) {
        // Define output array
        double[] output = new double[indices.length];

        // Copy elements
        for (int i = 0; i < indices.length; i++) {
            output[i] = array[indices[i]];
        }

        // Return output array
        return output;
    }

    /**
     * Find the index where an element should be inserted to maintain order in a sorted array.<br>
     * <p>
     * Find the index into a <b>sorted</b> array <code>array</code> such that, if the
     * <code>value</code> was inserted <b>before</b> the index, the order of elements in
     * <code>array</code> would be preserved.
     *
     * @param array The array of <b>sorted</b> elements. <b>No checks are in place to assert that
     *              this array is sorted</b>.
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
     * <code>frameLength = 3</code> and <code>hopLength = 2</code> will be framed into:
     * <ul>
     *     <li>
     *         <code>[[1, 3, 5, 7, 9], [2, 4, 6, 8, 10], [3, 5, 7, 9, 11]]</code> if
     *         <code>verticalFraming</code> is true
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
     * Calculate a 1-D maximum filter along the given axis.<br>
     * The lines of the array along the given axis are filtered with a maximum filter of given size.
     *
     * @param array The input array.
     * @param size  Length along which to calculate the 1-D maximum.
     * @return Maximum-filtered array with same shape as input.
     * @see <a href="https://bit.ly/37iRiK3">SciPy's implementation</a> of the MAXLIST algorithm.
     * This code is based off how that works. See also
     * <a href="https://stackoverflow.com/a/66808375">this StackOverflow answer</a> on how the
     * algorithm should work. Implementation is based off of Method 4 of
     * <a href="https://bit.ly/3LYIc3l">this GeeksForGeeks article</a>.
     */
    public static double[] maximumFilter1D(double[] array, int size) {
        // Get the number of elements in the array
        int numElem = array.length;

        // Pad array using the center reflect mode
        int padAmount;
        if (size % 2 == 1) {  // Odd size
            // Initial window is centered on FIRST element, and final window is centered on LAST element
            padAmount = (size - 1) / 2;
        } else {  // Even size
            // Initial window's center two elements are the FIRST element of the array
            padAmount = size / 2;
        }

        double[] paddedArray = padCenterReflect(array, numElem + 2 * padAmount);

        // Define maximum filter array and the double-ended queue (Deque)
        double[] maxFilterArray = new double[numElem];
        Deque<Integer> indicesQueue = new ArrayDeque<>();

        // Process first window
        for (int i = 0; i < size; i++) {
            // For every element, the previous smaller elements are useless so remove them from the indices queue
            while (indicesQueue.size() != 0 && paddedArray[i] >= paddedArray[indicesQueue.peekLast()]) {
                indicesQueue.removeLast();
            }

            // Add new element to the rear of the deque
            indicesQueue.addLast(i);
        }

        // Process the rest of the elements
        for (int i = size; i < numElem + 2 * padAmount; i++) {
            // The element at the front of the queue is the largest element of previous window, so add that into the
            // maximum filter array
            maxFilterArray[i - size] = paddedArray[indicesQueue.getFirst()];

            // Remove the elements which are out of this window
            while (indicesQueue.size() != 0 && indicesQueue.peekFirst() <= i - size) {
                indicesQueue.removeFirst();
            }

            // Remove all elements smaller than the currently being added element
            while (indicesQueue.size() != 0 && paddedArray[i] >= paddedArray[indicesQueue.peekLast()]) {
                indicesQueue.removeLast();
            }

            // Add new element to the rear of the deque
            indicesQueue.addLast(i);
        }

        // Add last window's maximum element to the maximum filter array if the size is odd
        if (size % 2 == 1) {
            maxFilterArray[numElem - 1] = paddedArray[indicesQueue.getFirst()];
        }

        // Return the maximum filter array
        return maxFilterArray;
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
     *
     * @param A The first matrix.
     * @param B The second matrix.
     * @return The multiplied matrix.
     * @throws LengthException If the matrix sizes are not suitable for multiplication.
     * @see <a href="https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm">This article</a>
     * on the naiive implementation on the Matrix Multiplication algorithm.
     */
    // Todo: see if can make this more efficient.
    public static Complex[][] matmul(Complex[][] A, Complex[][] B) {
        // Check if the matrices can be multiplied
        if (A[0].length != B.length) {
            throw new LengthException("Matrix sizes not suitable for multiplication");
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

    /**
     * Matrix multiply two real-numbered matrices.
     *
     * @param A The first matrix.
     * @param B The second matrix.
     * @return The multiplied matrix.
     * @throws LengthException If the matrix sizes are not suitable for multiplication.
     * @see <a href="https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm">This article</a>
     * on the naiive implementation on the Matrix Multiplication algorithm.
     */
    // Todo: see if can make this more efficient.
    public static double[][] matmul(double[][] A, double[][] B) {
        // Check if the matrices can be multiplied
        if (A[0].length != B.length) {
            throw new LengthException("Matrix sizes not suitable for multiplication");
        }

        // Otherwise, perform matrix multiplication
        int n = A.length;
        int m = A[0].length;
        int p = B[0].length;

        double[][] output = new double[A.length][B[0].length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                // Calculate the value of the current element
                double currElem = 0;
                for (int k = 0; k < m; k++) {
                    currElem += A[i][k] * B[k][j];
                }

                // Set the current element to the output matrix
                output[i][j] = currElem;
            }
        }

        // Return the output
        return output;
    }

    // Private methods

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
