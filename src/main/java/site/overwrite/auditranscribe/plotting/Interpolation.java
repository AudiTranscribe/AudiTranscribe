/*
 * Interpolation.java
 *
 * Created on 2022-03-14
 * Updated on 2022-05-25
 *
 * Description: Class containing interpolation functions.
 */

package site.overwrite.auditranscribe.plotting;

import site.overwrite.auditranscribe.exceptions.LengthException;

/**
 * Class containing interpolation functions.
 */
public class Interpolation {
    /**
     * Interpolate the array <code>array</code> to have the new shape <code>(lengthXNew,
     * lengthYNew)</code>.
     *
     * @param array         Original array of values.
     * @param lengthXNew    New number of sub-arrays present in the 2D array.
     * @param lengthYNew    New length of one subarray.
     * @param interpolation Interpolation method.
     * @return Interpolated version of the array.
     * @throws LengthException If the new lengths specified are shorter than the original lengths.
     */
    public static double[][] interpolate(
            double[][] array, int lengthXNew, int lengthYNew, InterpolationMethod interpolation
    ) {
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
        return interpolation.interpolation.interpolationHelper(X, Y, array, XNew, YNew);
    }
}
