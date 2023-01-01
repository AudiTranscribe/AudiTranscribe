/*
 * GeneralCosineWindow.java
 * Description: A general cosine signal window.
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

/**
 * A general cosine signal window.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Cosine-sum_windows">This article</a>
 * about the General Cosine window.
 */
public abstract class GeneralCosineWindow extends AbstractWindow {
    // Attributes
    double[] aCoefficients;  // `a` coefficients as seen in the article linked above

    // Protected methods

    /**
     * Method that generates the window value for a specific index <code>n</code>.
     *
     * @param n      Index of the window. <b>Note that this index is 1-indexed</b>.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>n</code>.
     */
    double windowFunc(int n, int length) {
        // Get the number of `a` coefficients
        int numACoefficients = aCoefficients.length;

        // Calculate the window value at index `n`
        double scale = 2 * Math.PI * n / (length - 1);
        double winVal = 0;

        for (int k = 0; k < numACoefficients; k++) {
            double changeInValue = aCoefficients[k] * Math.cos(k * scale);

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
