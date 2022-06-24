/*
 * InterpolationMethod.java
 *
 * Created on 2022-04-12
 * Updated on 2022-06-23
 *
 * Description: Enum that contains the different interpolation methods that can be used on a 2D
 *              array.
 */

package site.overwrite.auditranscribe.plotting;

import site.overwrite.auditranscribe.plotting.interpolation_methods.Bilinear;
import site.overwrite.auditranscribe.plotting.interpolation_methods.AbstractInterpolation;
import site.overwrite.auditranscribe.plotting.interpolation_methods.NearestNeighbour;

/**
 * Enum that contains the different interpolation methods that can be used on a 2D array.
 */
public enum InterpolationMethod {
    // Enum values
    NEAREST_NEIGHBOUR(new NearestNeighbour()),
    BILINEAR(new Bilinear());

    // Attributes
    final AbstractInterpolation interpolation;

    // Enum constructor
    InterpolationMethod(AbstractInterpolation interpolation) {
        this.interpolation = interpolation;
    }
}
