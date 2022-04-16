/*
 * InterpolationMethod.java
 *
 * Created on 2022-04-12
 * Updated on 2022-04-13
 *
 * Description: Interpolation methods enumerator.
 */

package site.overwrite.auditranscribe.plotting;

import site.overwrite.auditranscribe.plotting.interpolation_methods.Bilinear;
import site.overwrite.auditranscribe.plotting.interpolation_methods.AbstractInterpolation;
import site.overwrite.auditranscribe.plotting.interpolation_methods.NearestNeighbour;

/**
 * Interpolation method enum.<br>
 * Contains some interpolation methods to help adjust the image size.
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
