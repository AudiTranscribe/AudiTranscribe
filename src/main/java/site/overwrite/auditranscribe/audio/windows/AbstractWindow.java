/*
 * AbstractWindow.java
 *
 * Created on 2022-03-11
 * Updated on 2022-03-15
 *
 * Description: Abstract window class that implements most methods needed by window classes.
 */

package site.overwrite.auditranscribe.audio.windows;

import javafx.util.Pair;

/**
 * Abstract window class.
 */
public abstract class AbstractWindow {
    // Attributes
    double bandwidth;  // Bandwidths can be found here: http://librosa.org/doc/main/_modules/librosa/filters.html

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
        // Check if we need to extend the window
        Pair<Integer, Boolean> extendResponse = extend(length, symmetric);
        length = extendResponse.getKey();
        boolean truncateNeeded = extendResponse.getValue();

        // Generate the window
        double[] window = new double[length];
        for (int i = 0; i < length; i++) {
            window[i] = windowFunc(i + 1, length);
        }

        // Return the (possibly) truncated window
        return truncate(window, truncateNeeded);
    }

    // Protected methods

    /**
     * Method that generates the window value for a specific index <code>k</code>.
     *
     * @param k      Index of the window. Note that this is 1-indexed.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>k</code>.
     */
    double windowFunc(int k, int length) {
        return 0;
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
