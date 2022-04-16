/*
 * AbstractInterpolation.java
 *
 * Created on 2022-04-12
 * Updated on 2022-04-12
 *
 * Description: Abstract interpolation method class.
 */

package site.overwrite.auditranscribe.plotting.interpolation_methods;

/**
 * Abstract interpolation method class.
 */
public abstract class AbstractInterpolation {
    // Helper functions

    /**
     * Helper method to perform the interpolation.
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
    public abstract double[][] interpolationHelper(double[] X, double[] Y, double[][] Z, double[] XNew, double[] YNew);
}
