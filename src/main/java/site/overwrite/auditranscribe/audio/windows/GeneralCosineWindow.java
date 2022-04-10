/*
 * GeneralCosineWindow.java
 *
 * Created on 2022-03-12
 * Updated on 2022-04-10
 *
 * Description: Class that encapsulates the General Cosine window function.
 */

package site.overwrite.auditranscribe.audio.windows;

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
     * Method that generates the window value for a specific index <code>k</code>.
     *
     * @param k      Index of the window. Note that this is 1-indexed.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>k</code>.
     */
    double windowFunc(int k, int length) {
        // Get the number of `a` coefficients
        int numACoefficients = aCoefficients.length;

        // Calculate the window value at index `k`
        double winVal = 0;
        for (int i = 0; i < numACoefficients; i++) {
            // Determine the sign
            double sign = 1;
            if (i % 2 == 1) sign = -1;

            // Compute the window value proper
            winVal += sign * aCoefficients[i] * Math.cos(2 * Math.PI * i * k / length);
        }

        // Return the window value
        return winVal;
    }
}
