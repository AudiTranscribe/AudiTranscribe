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

package site.overwrite.auditranscribe.plotting.interpolation_methods;

/**
 * Abstract interpolation class.
 */
public abstract class AbstractInterpolation {
    // Public methods

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
