/*
 * Interpolation.java
 *
 * Created on 2022-03-14
 * Updated on 2022-03-15
 *
 * Description: Class containing interpolation functions.
 */

package site.overwrite.auditranscribe.plotting;

import site.overwrite.auditranscribe.utils.ArrayUtils;

import java.security.InvalidParameterException;

/**
 * Class containing interpolation functions.
 */
public class Interpolation {
    // Public methods

    /**
     * Interpolate the array <code>array</code> to have new shape <code>(lengthXNew,
     * lengthYNew)</code> bilinearly.
     *
     * @param array      Original array of values.
     * @param lengthXNew New number of subarrays present in the 2D array.
     * @param lengthYNew New length of one subarray.
     * @return Interpolated version of the array.
     * @throws InvalidParameterException If the new lengths specified are shorter than the original
     *                                   lengths.
     * @implNote <a href="https://en.wikipedia.org/wiki/Bilinear_interpolation#Computation">This
     * Wikipedia Article</a> on the Bilinear Interpolation's implementation
     */
    public static double[][] bilinear(double[][] array, int lengthXNew, int lengthYNew) {
        // Get the current `lengthX` and `lengthY`
        int lengthX = array.length;
        int lengthY = array[0].length;

        // Ensure that the new length is not shorter than the old length
        if (lengthXNew < lengthX) {
            throw new InvalidParameterException("New X length " + lengthXNew +
                    " shorter than original X length " + lengthX);
        }

        if (lengthYNew < lengthY) {
            throw new InvalidParameterException("New Y length " + lengthYNew +
                    " shorter than original Y length " + lengthY);
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
        return bilinear_helper(X, Y, array, XNew, YNew);
    }


    // Private methods

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
     * a value of <code>Z_new[i][j]</code>.
     * @implNote <a href="https://en.wikipedia.org/wiki/Bilinear_interpolation#Computation">This
     * Wikipedia Article</a> on the Bilinear Interpolation's implementation
     */
    private static double[][] bilinear_helper(double[] X, double[] Y, double[][] Z, double[] XNew, double[] YNew) {
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
                double xNew = XNew[iNew];
                double yNew = YNew[jNew];

                // Find the index of the closest element to `xNew` and `yNew` that is smaller than
                // them in their respective arrays
                // Todo: this seems inefficient; is there a better way?
                int i = ArrayUtils.searchSorted(X, xNew) - 1;  // Minus one to take the element replaced
                int j = ArrayUtils.searchSorted(Y, yNew) - 1;

                // Calculate actual indices
                int i1, i2, j1, j2;

                if (i < 0) {  // Handle weird negative index case
                    i1 = lengthX - 1;
                    i2 = 0;
                } else {
                    i1 = i;
                    i2 = i + 1;
                }

                if (j < 0) {
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
                double coefficient = 1. / (x2 - x1);
                double fxy1 = coefficient * ((x2 - xNew) * Z[i1][j1] + (xNew - x1) * Z[i2][j1]);
                double fxy2 = coefficient * ((x2 - xNew) * Z[i1][j2] + (xNew - x1) * Z[i2][j2]);

                // Proceed to interpolate in the y direction
                coefficient = 1. / (y2 - y1);
                double fxy = coefficient * ((y2 - yNew) * fxy1 + (yNew - y1) * fxy2);

                // Set the value in the output array
                ZNew[iNew][jNew] = fxy;
            }
        }

        // Return the output
        return ZNew;
    }
}
