/*
 * AbstractInterpolation.java
 *
 * Created on 2022-04-12
 * Updated on 2022-06-23
 *
 * Description: Abstract interpolation class.
 */

package site.overwrite.auditranscribe.plotting.interpolation_methods;

/**
 * Abstract interpolation class.
 */
public abstract class AbstractInterpolation {
    // Helper functions

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
    public abstract double[][] interpolationHelper(double[] X, double[] Y, double[][] Z, double[] XNew, double[] YNew);
}
