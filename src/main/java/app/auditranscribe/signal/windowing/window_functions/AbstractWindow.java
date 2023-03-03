/*
 * AbstractWindow.java
 * Description: Abstract window class that implements methods needed by signal windows.
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

package app.auditranscribe.signal.windowing.window_functions;

import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.generic.tuples.Pair;

/**
 * Abstract window class that implements methods needed by signal windows.
 */
public abstract class AbstractWindow {
    // Attributes
    double bandwidth;  // Signal window bandwidth

    // Getter/Setter Methods

    /**
     * @return Bandwidth of the signal window.
     */
    public double getBandwidth() {
        return bandwidth;
    }

    // Public methods

    /**
     * Generates the signal window of the specified length.
     *
     * @param length    Length of window to generate.
     * @param symmetric Whether the window is Discrete Fourirer Transform (DFT)-even symmetric.
     * @return The window array.
     */
    public double[] generateWindow(int length, boolean symmetric) {
        // Check if the length is valid
        if (checkForSmallLengths(length)) {
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

    // Package-protected methods

    /**
     * Method that generates the signal window's value for a specific index <code>k</code>.
     *
     * @param k      Index of the window.<br>
     *               <b>Note that this index is 1-indexed</b>.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>k</code>.
     */
    abstract double windowFunc(int k, int length);

    // Private methods

    /**
     * Helper function to check for small window lengths.
     *
     * @return A boolean; returns <code>true</code> if the provided length is 0 or 1 and
     * <code>false</code> otherwise.
     * @throws LengthException If the signal window length is negative.
     */
    private boolean checkForSmallLengths(int length) throws NegativeArraySizeException {
        if (length < 0) {
            throw new LengthException("Signal window length must be a non-negative integer");
        } else {
            return length <= 1;  // So it returns `true` if length is 0 or 1
        }
    }

    /**
     * Helper method that extends the signal window by 1 sample if needed for Discrete Fourier
     * Transform (DFT)-even symmetry.
     *
     * @param length    Length of current window.
     * @param symmetric Whether it is DFT-even symmetric.
     * @return Pair of values. First value is the new length. Second value is whether truncation
     * is needed.
     */
    private Pair<Integer, Boolean> extend(int length, boolean symmetric) {
        if (symmetric) return new Pair<>(length, false);
        return new Pair<>(length + 1, true);
    }

    /**
     * Truncate window by 1 sample if needed for Discrete Fourier Transform (DFT)-even symmetry.
     *
     * @param window         Window array.
     * @param truncateNeeded Whether truncation is needed or not.
     * @return The (possibly) truncated window array.
     */
    private double[] truncate(double[] window, boolean truncateNeeded) {
        if (truncateNeeded) {
            double[] output = new double[window.length - 1];
            System.arraycopy(window, 0, output, 0, window.length - 1);
            return output;
        } else {
            return window;
        }
    }
}
