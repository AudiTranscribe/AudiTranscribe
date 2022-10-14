/*
 * AbstractWindow.java
 * Description: Abstract window class that implements most methods needed by window classes.
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

package site.overwrite.auditranscribe.audio.window_functions;

import site.overwrite.auditranscribe.generic.tuples.Pair;

/**
 * Abstract window class that implements most methods needed by window classes.
 */
public abstract class AbstractWindow {
    // Attributes
    double bandwidth;  // Bandwidths can be found at http://librosa.org/doc/main/_modules/librosa/filters.html

    // Getter/Setter Methods

    /**
     * Gets the bandwidth of the window.
     *
     * @return Bandwidth of the window.
     */
    public double getBandwidth() {
        return bandwidth;
    }

    // Public methods

    /**
     * Generates the window of the specified length.
     *
     * @param length    Length of window to generate.
     * @param symmetric Whether the window is symmetric or not.
     * @return The window array.
     */
    public double[] generateWindow(int length, boolean symmetric) {
        // Check if the length is valid
        if (lengthGuard(length)) {
            // Special case: return only ones
            double[] specialCaseArray = new double[length];
            if (length == 1) specialCaseArray[0] = 1;
            return specialCaseArray;
        }

        // Check if we need to extend the window
        Pair<Integer, Boolean> extendResponse = extend(length, symmetric);
        int newLength = extendResponse.value0();
        boolean truncateNeeded = extendResponse.value1();

        // Generate the window
        double[] window = new double[newLength];
        for (int n = 0; n < newLength; n++) {
            window[n] = windowFunc(n, newLength);
        }

        // Return the (possibly) truncated window
        return truncate(window, truncateNeeded);
    }

    // Protected methods

    /**
     * Method that generates the window value for a specific index <code>k</code>.
     *
     * @param k      Index of the window. <b>Note that this index is 1-indexed</b>.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>k</code>.
     */
    abstract double windowFunc(int k, int length);

    /**
     * Helper function to handle small or incorrect window lengths.
     *
     * @return A boolean whether the length is smaller than or equal to 1.
     * @throws NegativeArraySizeException If the length is negative.
     */
    boolean lengthGuard(int length) throws NegativeArraySizeException {
        if (length < 0) {
            throw new NegativeArraySizeException("Window length must be a non-negative integer");
        } else {
            return (length <= 1);  // So it returns `true` if length is 0 or 1
        }
    }

    /**
     * Extend window by 1 sample if needed for DFT-even symmetry.
     *
     * @param length    Length of current window.
     * @param symmetric Whether it is symmetric or not.
     * @return Pair of values. First value is the new length. Second value is whether truncation
     * is needed.
     */
    Pair<Integer, Boolean> extend(int length, boolean symmetric) {
        if (symmetric) return new Pair<>(length, false);
        return new Pair<>(length + 1, true);
    }

    /**
     * Truncate window by 1 sample if needed for DFT-even symmetry.
     *
     * @param window         Window array.
     * @param truncateNeeded Whether truncation is needed or not.
     * @return The (possibly) truncated window array.
     */
    double[] truncate(double[] window, boolean truncateNeeded) {
        if (truncateNeeded) {
            double[] output = new double[window.length - 1];
            System.arraycopy(window, 0, output, 0, window.length - 1);
            return output;
        } else {
            return window;
        }
    }
}
