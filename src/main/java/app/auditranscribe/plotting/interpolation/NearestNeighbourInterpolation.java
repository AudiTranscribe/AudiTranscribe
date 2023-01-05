/*
 * NearestNeighbourInterpolation.java
 * Description: Class that handles the nearest neighbour interpolation method.
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

package app.auditranscribe.plotting.interpolation;

/**
 * Class that handles the nearest neighbour interpolation method.
 */
public class NearestNeighbourInterpolation extends AbstractInterpolation {
    // Overridden methods

    /**
     * Helper method to perform the nearest neighbour interpolation.
     *
     * @param X    Array of x values.
     * @param Y    Array of y values.
     * @param Z    Array of values.<br>
     *             The point <code>(X[i], Y[j])</code> has a value of <code>Z[i][j]</code>.
     * @param XNew Array of new x values.
     * @param YNew Array of new y values.
     * @return Array of new values, `ZNew`, where the point <code>(XNew[i], YNew[j])</code> has
     * a value of <code>Z_new[i][j]</code>.
     */
    @Override
    public double[][] interpolationFunction(
            double[] X, double[] Y, double[][] Z, double[] XNew, double[] YNew
    ) {
        // Get the lengths of the arrays
        int lengthXNew = XNew.length;
        int lengthYNew = YNew.length;

        // Define final interpolated array
        double[][] ZNew = new double[lengthXNew][lengthYNew];

        // Run interpolation algorithm
        for (int iNew = 0; iNew < lengthXNew; iNew++) {
            for (int jNew = 0; jNew < lengthYNew; jNew++) {
                // Get the new `x` and `y` values
                double xNew = XNew[iNew];
                double yNew = YNew[jNew];

                // Separate the `xNew` and `yNew` into integer and decimal parts
                int xNewIntPart = (int) xNew;
                double xNewDecimalPart = xNew - xNewIntPart;

                int yNewIntPart = (int) yNew;
                double yNewDecimalPart = yNew - yNewIntPart;

                // Round the `xNew` and `yNew` to get the nearest neighbour index
                // (Note: we ROUND UP if and only if the decimal part is STRICTLY MORE THAN 0.5)
                int i = xNewIntPart;
                int j = yNewIntPart;

                if (xNewDecimalPart > 0.5) {
                    i += 1;
                }

                if (yNewDecimalPart > 0.5) {
                    j += 1;
                }

                // Use these indices values for the final array
                ZNew[iNew][jNew] = Z[i][j];
            }
        }

        // Return the output
        return ZNew;
    }
}
