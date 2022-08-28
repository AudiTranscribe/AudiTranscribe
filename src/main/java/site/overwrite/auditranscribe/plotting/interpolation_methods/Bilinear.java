/*
 * Bilinear.java
 * Description: Class that handles the bilinear interpolation method.
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

package site.overwrite.auditranscribe.plotting.interpolation_methods;

import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.MathUtils;

/**
 * Class that handles the bilinear interpolation method.
 */
public class Bilinear extends AbstractInterpolation {
    /**
     * Helper method to perform the bilinear interpolation.
     *
     * @param X    Array of x values.
     * @param Y    Array of y values.
     * @param Z    Array of values.<br>
     *             The point <code>(X[i], Y[j])</code> has a value of <code>Z[i][j]</code>.
     * @param XNew Array of new x values.
     * @param YNew Array of new y values.
     * @return Array of new values, `ZNew`, where the point <code>(XNew[i], YNew[j])</code> has
     * a value of <code>ZNew[i][j]</code>.
     * @implNote <a href="https://en.wikipedia.org/wiki/Bilinear_interpolation#Computation">This
     * Wikipedia Article</a> on the Bilinear Interpolation's implementation.
     */
    @Override
    public double[][] interpolationHelper(
            double[] X, double[] Y, double[][] Z, double[] XNew, double[] YNew
    ) {
        // Get the lengths of the arrays
        int lengthX = X.length;
        int lengthY = Y.length;
        int lengthXNew = XNew.length;
        int lengthYNew = YNew.length;

        // Define final interpolated array
        double[][] ZNew = new double[lengthXNew][lengthYNew];

        // Run interpolation algorithm
        for (int iNew = 0; iNew < lengthXNew; iNew++) {
            for (int jNew = 0; jNew < lengthYNew; jNew++) {
                // Get the new `x` and `y` values
                double xNew = MathUtils.round(XNew[iNew], 10);  // Round to 10 dp to account for possible imprecision
                double yNew = MathUtils.round(YNew[jNew], 10);  // Same goes for `yNew`

                // Find the index of the closest element to `xNew` and `yNew` that is smaller than
                // them in their respective arrays
                int i = ArrayUtils.searchSorted(X, xNew) - 1;  // Minus one to take the element replaced
                int j = ArrayUtils.searchSorted(Y, yNew) - 1;

                // Calculate actual indices
                int i1, i2, j1, j2;

                if (i < 0) {  // Handle weird index case; note `i` != `lengthX - 1` as `xNew` is bounded by max X
                    i1 = lengthX - 1;
                    i2 = 0;
                } else {
                    i1 = i;
                    i2 = i + 1;
                }

                if (j < 0) {  // Handle weird index case
                    j1 = lengthY - 1;
                    j2 = 0;
                } else {
                    j1 = j;
                    j2 = j + 1;
                }

                // Get the known points
                double x1 = X[i1];
                double x2 = X[i2];

                double y1 = Y[j1];
                double y2 = Y[j2];

                // Get the values of `f(x, y1)` and `f(x, y2)`
                double coefficient, fxy1, fxy2, fxy;
                if (x1 != x2) {
                    coefficient = 1. / (x2 - x1);
                    fxy1 = coefficient * ((x2 - xNew) * Z[i1][j1] + (xNew - x1) * Z[i2][j1]);
                    fxy2 = coefficient * ((x2 - xNew) * Z[i1][j2] + (xNew - x1) * Z[i2][j2]);
                } else {  // Have same x-coordinate
                    fxy1 = Z[i1][j1];
                    fxy2 = Z[i1][j2];
                }

                // Proceed to interpolate in the y direction
                if (y1 != y2) {
                    coefficient = 1. / (y2 - y1);
                    fxy = coefficient * ((y2 - yNew) * fxy1 + (yNew - y1) * fxy2);
                } else {  // Have same y-coordinate
                    fxy = fxy1;
                }

                // Set the value in the output array
                ZNew[iNew][jNew] = fxy;
            }
        }

        // Return the output
        return ZNew;
    }
}
