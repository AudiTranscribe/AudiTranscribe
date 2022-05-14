/*
 * GeneralCosineWindow.java
 *
 * Created on 2022-03-12
 * Updated on 2022-05-14
 *
 * Description: Class that encapsulates the General Cosine window function.
 */

package site.overwrite.auditranscribe.audio.window_functions;

/**
 * General Cosine window.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Cosine-sum_windows">This article</a>
 * about the General Cosine window.
 */
public abstract class GeneralCosineWindow extends AbstractWindow {
    // Attributes
    double[] aCoefficients;  // `a` coefficients

    // Protected methods

    /**
     * Method that generates the window value for a specific index <code>n</code>.
     *
     * @param n      Index of the window. Note that this is 1-indexed.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>n</code>.
     */
    double windowFunc(int n, int length) {
        // Get the number of `a` coefficients
        int numACoefficients = aCoefficients.length;

        // Calculate the window value at index `n`
        double winVal = 0;
        for (int k = 0; k < numACoefficients; k++) {
            // Determine the change in window value
            double changeInValue = aCoefficients[k] * Math.cos(2 * Math.PI * k * n / (length - 1));

            // Determine whether to add or subtract from the window value
            if (k % 2 == 0) {
                winVal += changeInValue;
            } else {
                winVal -= changeInValue;
            }
        }

        // Return the window value
        return winVal;
    }
}
