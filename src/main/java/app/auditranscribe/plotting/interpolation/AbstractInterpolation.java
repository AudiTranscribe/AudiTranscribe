/*
 * AbstractInterpolation.java
 * Description: Abstract interpolation class.
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

import app.auditranscribe.generic.exceptions.LengthException;

/**
 * Abstract interpolation class.
 */
public abstract class AbstractInterpolation {
    // Public methods

    /**
     * Interpolate <code>array</code> to have the new shape <code>(lengthXNew, lengthYNew)</code>.
     *
     * @param array      Original array of values.
     * @param lengthXNew New number of sub-arrays present in the 2D array.
     * @param lengthYNew New length of one subarray.
     * @return Interpolated array.
     * @throws LengthException If the new lengths specified are shorter than the original lengths.
     */
    public double[][] interpolate(double[][] array, int lengthXNew, int lengthYNew) {
        // Get the current `lengthX` and `lengthY`
        int lengthX = array.length;
        int lengthY = array[0].length;

        // Ensure that the new length is not shorter than the old length
        if (lengthXNew < lengthX) {
            throw new LengthException("New X length " + lengthXNew + " shorter than original X length " + lengthX);
        }

        if (lengthYNew < lengthY) {
            throw new LengthException("New Y length " + lengthYNew + " shorter than original Y length " + lengthY);
        }

        // Calculate ratio of the new lengths to the old lengths
        double xRatio = (double) (lengthX - 1) / (lengthXNew - 1);
        double yRatio = (double) (lengthY - 1) / (lengthYNew - 1);

        // Create arrays to use in the helper function
        double[] X = new double[lengthX];
        for (int x = 0; x < lengthX; x++) X[x] = x;

        double[] Y = new double[lengthY];
        for (int y = 0; y < lengthY; y++) Y[y] = y;

        double[] XNew = new double[lengthXNew];
        for (int x = 0; x < lengthXNew; x++) XNew[x] = x * xRatio;

        double[] YNew = new double[lengthYNew];
        for (int y = 0; y < lengthYNew; y++) YNew[y] = y * yRatio;

        // Now use the helper function to generate the output matrix
        return interpolationFunction(X, Y, array, XNew, YNew);
    }

    // Package-protected methods
    /**
     * Method that performs the interpolation.
     *
     * @param X    Array of x values.
     * @param Y    Array of y values.
     * @param Z    Array of values.<br>
     *             The point <code>(X[i], Y[j])</code> has a value of <code>Z[i][j]</code>.
     * @param XNew Array of new x values.
     * @param YNew Array of new y values.
     * @return Array of new values, <code>ZNew</code>, where the point <code>(XNew[i], YNew[j])</code> has
     * a value of <code>ZNew[i][j]</code>.
     */
    abstract double[][] interpolationFunction(double[] X, double[] Y, double[][] Z, double[] XNew, double[] YNew);
}
